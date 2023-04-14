package com.ejemplos.tus.server.core.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.ejemplos.tus.server.exception.InvalidContentTypeException;
import com.ejemplos.tus.server.exception.InvalidTusResumableException;
import com.ejemplos.tus.server.upload.UploadStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;

public class TusResumableValidatorTest {

    private MockHttpServletRequest servletRequest;
    private TusResumableValidator validator;
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new TusResumableValidator();
    }

    @Test
    public void validateNoVersion() throws Exception {
        Throwable exception =
                assertThrows(InvalidTusResumableException.class, () -> {

                    validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
                });
    }

    @Test
    public void validateInvalidVersion() throws Exception {
        servletRequest.addHeader(HttpHeader.TUS_RESUMABLE, "2.0.0");
        Throwable exception =
                assertThrows(InvalidTusResumableException.class, () -> {
                    validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
                });
    }

    @Test
    public void validateValid() throws Exception {
        servletRequest.addHeader(HttpHeader.TUS_RESUMABLE, "1.0.0");
        try {
            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void validateNullMethod() throws Exception {
        servletRequest.addHeader(HttpHeader.TUS_RESUMABLE, "1.0.0");
        try {
            validator.validate(null, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void supports() throws Exception {
        assertThat(validator.supports(HttpMethod.GET), is(false));
        assertThat(validator.supports(HttpMethod.POST), is(true));
        assertThat(validator.supports(HttpMethod.PUT), is(true));
        assertThat(validator.supports(HttpMethod.DELETE), is(true));
        assertThat(validator.supports(HttpMethod.HEAD), is(true));
        assertThat(validator.supports(HttpMethod.OPTIONS), is(false));
        assertThat(validator.supports(HttpMethod.PATCH), is(true));
        assertThat(validator.supports(null), is(true));
    }
}