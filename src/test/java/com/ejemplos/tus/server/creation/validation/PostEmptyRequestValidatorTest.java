package com.ejemplos.tus.server.creation.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.ejemplos.tus.server.exception.InvalidContentLengthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;

class PostEmptyRequestValidatorTest {

    private PostEmptyRequestValidator validator;

    private MockHttpServletRequest servletRequest;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new PostEmptyRequestValidator();
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
    void validateMissingContentLength() throws Exception {

        // When we validate the request
        try {
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

    @Test
    void validateContentLengthZero() throws Exception {
        servletRequest.addHeader(HttpHeader.CONTENT_LENGTH, 0L);

        // When we validate the request
        try {
            validator.validate(HttpMethod.POST, servletRequest, null, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

    @Test
    void validateContentLengthNotZero() {
        assertThrows(InvalidContentLengthException.class, () -> {

            servletRequest.addHeader(HttpHeader.CONTENT_LENGTH, 10L);

            // When we validate the request
            validator.validate(HttpMethod.POST, servletRequest, null, null);

            // Expect a InvalidContentLengthException
        });
    }
}