package com.ejemplos.tus.server.checksum.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ejemplos.tus.server.exception.ChecksumAlgorithmNotSupportedException;
import com.ejemplos.tus.server.upload.UploadStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;

@ExtendWith(MockitoExtension.class)
class ChecksumAlgorithmValidatorTest {

    private ChecksumAlgorithmValidator validator;

    private MockHttpServletRequest servletRequest;

    @Mock
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = spy(new MockHttpServletRequest());
        validator = new ChecksumAlgorithmValidator();
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
        servletRequest.addHeader(HttpHeader.UPLOAD_CHECKSUM, "sha1 1234567890");

        validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);

        verify(servletRequest, times(1)).getHeader(HttpHeader.UPLOAD_CHECKSUM);
    }

    @Test
    void testNoHeader() throws Exception {
        try {
            validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    void testInvalidHeader() {
        assertThrows(ChecksumAlgorithmNotSupportedException.class, () -> {
            servletRequest.addHeader(HttpHeader.UPLOAD_CHECKSUM, "test 1234567890");

            validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
        });
    }

}