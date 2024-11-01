package com.ejemplos.tus.server.creation.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.ejemplos.tus.server.exception.InvalidUploadLengthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;

/**
 * The request MUST include one of the following headers:
 * a) Upload-Length to indicate the size of an entire upload in bytes.
 * b) Upload-Defer-Length: 1 if upload size is not known at the time.
 */
class UploadDeferLengthValidatorTest {

    private UploadDeferLengthValidator validator;

    private MockHttpServletRequest servletRequest;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new UploadDeferLengthValidator();
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
    void validateUploadLengthPresent() throws Exception {
        servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 300L);

        // When we validate the request
        try {
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

    @Test
    void validateUploadDeferLength1Present() throws Exception {
        servletRequest.addHeader(HttpHeader.UPLOAD_DEFER_LENGTH, 1);

        // When we validate the request
        try {
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

    @Test
    void validateUploadLengthAndUploadDeferLength1Present() {
        assertThrows(InvalidUploadLengthException.class, () -> {

            servletRequest.addHeader(HttpHeader.UPLOAD_DEFER_LENGTH, 1);
            servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 300L);

            // When we validate the request
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        });
        // Expect an InvalidUploadLengthException
    }

    @Test
    void validateUploadDeferLengthNot1() {
        assertThrows(InvalidUploadLengthException.class, () -> {

            servletRequest.addHeader(HttpHeader.UPLOAD_DEFER_LENGTH, 2);

            // When we validate the request
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        });
        // Expect an InvalidUploadLengthException
    }

    @Test
    void validateUploadLengthNotPresent() {
        assertThrows(InvalidUploadLengthException.class, () -> {

            // When we validate the request
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        });
        // Expect an InvalidUploadLengthException
    }

    @Test
    void validateUploadLengthNotPresentOnFinal() throws Exception {
        servletRequest.addHeader(HttpHeader.UPLOAD_CONCAT, "final;1234 5678");

        // When we validate the request
        try {
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

    @Test
    void validateUploadLengthNotNumeric() {
        assertThrows(InvalidUploadLengthException.class, () -> {

            servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, "TEST");

            // When we validate the request
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        });
        // Expect an InvalidUploadLengthException
    }
}