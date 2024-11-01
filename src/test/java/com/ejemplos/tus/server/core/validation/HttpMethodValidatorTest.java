package com.ejemplos.tus.server.core.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.ejemplos.tus.server.exception.UnsupportedMethodException;
import com.ejemplos.tus.server.upload.UploadStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpMethod;

class HttpMethodValidatorTest {

    private MockHttpServletRequest servletRequest;
    private HttpMethodValidator validator;
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new HttpMethodValidator();
    }

    @Test
    void validateValid() throws Exception {
        try {
            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    void validateInvalid() {
        assertThrows(UnsupportedMethodException.class, () -> {
            validator.validate(null, servletRequest, uploadStorageService, null);
        });
    }

    @Test
    void supports() {
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