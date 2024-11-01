package com.ejemplos.tus.server.concatenation.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.ejemplos.tus.server.upload.UploadId;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.upload.UploadType;
import com.ejemplos.tus.server.exception.PatchOnFinalUploadNotAllowedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpMethod;

@ExtendWith(MockitoExtension.class)
class PatchFinalUploadValidatorTest {

    private PatchFinalUploadValidator validator;

    private MockHttpServletRequest servletRequest;

    @Mock
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new PatchFinalUploadValidator();
    }

    @Test
    void supports() {
        assertThat(validator.supports(HttpMethod.GET), is(false));
        assertThat(validator.supports(HttpMethod.POST), is(false));
        assertThat(validator.supports(HttpMethod.PUT), is(false));
        assertThat(validator.supports(HttpMethod.DELETE), is(false));
        assertThat(validator.supports(HttpMethod.HEAD), is(false));
        assertThat(validator.supports(HttpMethod.OPTIONS), is(false));
        assertThat(validator.supports(HttpMethod.PATCH), is(true));
        assertThat(validator.supports(null), is(false));
    }

    @Test
    void testValid() throws Exception {
        UploadInfo info1 = new UploadInfo();
        info1.setId(new UploadId(UUID.randomUUID()));
        info1.setUploadType(UploadType.REGULAR);

        UploadInfo info2 = new UploadInfo();
        info2.setId(new UploadId(UUID.randomUUID()));
        info2.setUploadType(UploadType.PARTIAL);

        UploadInfo info3 = new UploadInfo();
        info3.setId(new UploadId(UUID.randomUUID()));
        info3.setUploadType(null);

        when(uploadStorageService.getUploadInfo(eq(info1.getId().toString()),
                nullable(String.class))).thenReturn(info1);
        when(uploadStorageService.getUploadInfo(eq(info2.getId().toString()),
                nullable(String.class))).thenReturn(info2);
        when(uploadStorageService.getUploadInfo(eq(info3.getId().toString()),
                nullable(String.class))).thenReturn(info3);

        // When we validate the requests
        try {
            servletRequest.setRequestURI(info1.getId().toString());
            validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);

            servletRequest.setRequestURI(info2.getId().toString());
            validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);

            servletRequest.setRequestURI(info3.getId().toString());
            validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }

        // No exception is thrown
    }

    @Test
    void testValidNotFound() throws Exception {
        try {
            // When we validate the request
            servletRequest.setRequestURI("/upload/test");
            validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    void testInvalidFinal() throws Exception {
        UploadInfo info1 = new UploadInfo();
        info1.setId(new UploadId(UUID.randomUUID()));
        info1.setUploadType(UploadType.CONCATENATED);

        when(uploadStorageService.getUploadInfo(eq(info1.getId().toString()),
                nullable(String.class))).thenReturn(info1);

        // When we validate the request
        assertThrows(PatchOnFinalUploadNotAllowedException.class, () -> {
            servletRequest.setRequestURI(info1.getId().toString());
            validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
        });
    }
}