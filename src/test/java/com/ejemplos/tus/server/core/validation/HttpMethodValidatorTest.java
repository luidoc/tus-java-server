package com.ejemplos.tus.server.core.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.ejemplos.tus.server.exception.InvalidContentTypeException;
import com.ejemplos.tus.server.exception.UnsupportedMethodException;
import com.ejemplos.tus.server.upload.UploadIdFactory;
import com.ejemplos.tus.server.upload.UploadStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpMethod;

public class HttpMethodValidatorTest {

    private MockHttpServletRequest servletRequest;
    private HttpMethodValidator validator;
    private UploadStorageService uploadStorageService;
    private UploadIdFactory idFactory;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new HttpMethodValidator();
    }

    @Test
    public void validateValid() throws Exception {
        try {
            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void validateInvalid() throws Exception {
        Throwable exception =
                assertThrows(UnsupportedMethodException.class, () -> {
                    validator.validate(null, servletRequest, uploadStorageService, null);
                });
    }

    @Test
    public void supports() throws Exception {
        assertThat(validator.supports(HttpMethod.GET), is(true));
        assertThat(validator.supports(HttpMethod.POST), is(true));
        assertThat(validator.supports(HttpMethod.PUT), is(true));
        assertThat(validator.supports(HttpMethod.DELETE), is(true));
        assertThat(validator.supports(HttpMethod.HEAD), is(true));
        assertThat(validator.supports(HttpMethod.OPTIONS), is(true));
        assertThat(validator.supports(HttpMethod.PATCH), is(true));
        assertThat(validator.supports(null), is(true));
    }
}