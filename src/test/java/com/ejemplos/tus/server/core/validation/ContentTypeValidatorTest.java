package com.ejemplos.tus.server.core.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.ejemplos.tus.server.exception.InvalidContentTypeException;
import com.ejemplos.tus.server.exception.UploadChecksumMismatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;


public class ContentTypeValidatorTest {

    private ContentTypeValidator validator;

    private MockHttpServletRequest servletRequest;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new ContentTypeValidator();
    }

    @Test
    public void validateValid() throws Exception {
        servletRequest.addHeader(HttpHeader.CONTENT_TYPE, ContentTypeValidator.APPLICATION_OFFSET_OCTET_STREAM);

        try {
            validator.validate(HttpMethod.PATCH, servletRequest, null, null);
        } catch (Exception ex) {
            fail();
        }

        //No exception is thrown
    }

    @Test
    public void validateInvalidHeader() throws Exception {
        servletRequest.addHeader(HttpHeader.CONTENT_TYPE, "application/octet-stream");
        Throwable exception =
                assertThrows(InvalidContentTypeException.class, () -> {
                            validator.validate(HttpMethod.PATCH, servletRequest, null, null);
                        });
        //Expect a InvalidContentTypeException exception
    }

    @Test
    public void validateMissingHeader() throws Exception {
        //We don't set the header
        //servletRequest.addHeader(HttpHeader.CONTENT_TYPE, ContentTypeValidator.APPLICATION_OFFSET_OCTET_STREAM);
        Throwable exception =
                assertThrows(InvalidContentTypeException.class, () -> {

                            validator.validate(HttpMethod.PATCH, servletRequest, null, null);
                        });
        //Expect a InvalidContentTypeException exception
    }

    @Test
    public void supports() throws Exception {
        assertThat(validator.supports(HttpMethod.GET), is(false));
        assertThat(validator.supports(HttpMethod.POST), is(false));
        assertThat(validator.supports(HttpMethod.PUT), is(false));
        assertThat(validator.supports(HttpMethod.DELETE), is(false));
        assertThat(validator.supports(HttpMethod.HEAD), is(false));
        assertThat(validator.supports(HttpMethod.OPTIONS), is(false));
        assertThat(validator.supports(HttpMethod.PATCH), is(true));
        assertThat(validator.supports(null), is(false));
    }

}