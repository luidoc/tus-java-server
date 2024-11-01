package com.ejemplos.tus.server.concatenation.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.ejemplos.tus.server.exception.UploadLengthNotAllowedOnConcatenationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;

class NoUploadLengthOnFinalValidatorTest {

    private NoUploadLengthOnFinalValidator validator;

    private MockHttpServletRequest servletRequest;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new NoUploadLengthOnFinalValidator();
    }

    @Test
    void supports() {
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
    void validateFinalUploadValid() throws Exception {
        servletRequest.addHeader(HttpHeader.UPLOAD_CONCAT, "final;12345 235235 253523");

        // When we validate the request
        try {
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

    @Test
    void validateFinalUploadInvalid() {
        assertThrows(UploadLengthNotAllowedOnConcatenationException.class, () -> {
            servletRequest.addHeader(HttpHeader.UPLOAD_CONCAT, "final;12345 235235 253523");
            servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, "10L");

            // When we validate the request
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        });
    }

    @Test
    void validateNotFinal1() throws Exception {
        servletRequest.addHeader(HttpHeader.UPLOAD_CONCAT, "partial");
        servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, "10L");

        // When we validate the request
        try {
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

    @Test
    void validateNotFinal2() throws Exception {

        // When we validate the request
        try {
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

}