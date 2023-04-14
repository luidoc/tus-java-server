package com.ejemplos.tus.server.concatenation.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.ejemplos.tus.server.exception.InvalidPartialUploadIdException;
import com.ejemplos.tus.server.upload.UploadId;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PartialUploadsExistValidatorTest {

    private PartialUploadsExistValidator validator;

    private MockHttpServletRequest servletRequest;

    @Mock
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new PartialUploadsExistValidator();
    }

    @Test
    public void supports() throws Exception {
        assertThat(validator.supports(HttpMethod.GET), is(false));
        assertThat(validator.supports(HttpMethod.POST), is(true));
        assertThat(validator.supports(HttpMethod.PUT), is(false));
        assertThat(validator.supports(HttpMethod.DELETE), is(false));
        assertThat(validator.supports(HttpMethod.HEAD), is(false));
        assertThat(validator.supports(HttpMethod.OPTIONS), is(false));
        assertThat(validator.supports(HttpMethod.PATCH), is(false));
        assertThat(validator.supports(null), is(false));
    }

    @Test
    public void testValid() throws Exception {
        UploadInfo info1 = new UploadInfo();
        info1.setId(new UploadId(UUID.randomUUID()));

        UploadInfo info2 = new UploadInfo();
        info2.setId(new UploadId(UUID.randomUUID()));

        when(uploadStorageService.getUploadInfo(info1.getId().toString(), null)).thenReturn(info1);
        when(uploadStorageService.getUploadInfo(info2.getId().toString(), null)).thenReturn(info2);

        servletRequest.addHeader(HttpHeader.UPLOAD_CONCAT, String.format("final; %s %s", info1.getId(), info2.getId()));

        //When we validate the request
        validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);

        //No exception is thrown
    }

    @Test
    public void testInvalidUploadNotFound() throws Exception {
        UploadInfo info1 = new UploadInfo();
        info1.setId(new UploadId(UUID.randomUUID()));

        when(uploadStorageService.getUploadInfo(info1.getId())).thenReturn(info1);

        servletRequest.addHeader(HttpHeader.UPLOAD_CONCAT,
                String.format("final; %s %s", info1.getId(), UUID.randomUUID()));

        //When we validate the request
        Throwable exception =
                assertThrows(InvalidPartialUploadIdException.class, () -> {
                    validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
                });
    }

    @Test
    public void testInvalidId() throws Exception {
        UploadInfo info1 = new UploadInfo();
        info1.setId(new UploadId(UUID.randomUUID()));
        Throwable exception =
                assertThrows(InvalidPartialUploadIdException.class, () -> {
                    when(uploadStorageService.getUploadInfo(info1.getId().toString(), null)).thenReturn(info1);

                    servletRequest.addHeader(HttpHeader.UPLOAD_CONCAT, String.format("final; %s %s", info1.getId(), "test"));

                    //When we validate the request
                    validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
                });
    }

    @Test
    public void testInvalidNoUploads1() throws Exception {
        servletRequest.addHeader(HttpHeader.UPLOAD_CONCAT, "final;   ");

        //When we validate the request
        Throwable exception =
                assertThrows(InvalidPartialUploadIdException.class, () -> {
                            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
                        });

        //No Exception is thrown
    }

    @Test
    public void testInvalidNoUploads2() throws Exception {
        servletRequest.addHeader(HttpHeader.UPLOAD_CONCAT, "final;");

        //When we validate the request
        Throwable exception =
                assertThrows(InvalidPartialUploadIdException.class, () -> {
                            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
                        });

        //No Exception is thrown
    }
}