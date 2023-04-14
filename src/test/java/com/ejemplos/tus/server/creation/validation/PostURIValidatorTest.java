package com.ejemplos.tus.server.creation.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import com.ejemplos.tus.server.exception.InvalidContentTypeException;
import com.ejemplos.tus.server.exception.PostOnInvalidRequestURIException;
import com.ejemplos.tus.server.upload.UploadStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpMethod;

@ExtendWith(MockitoExtension.class)
public class PostURIValidatorTest {

    private PostURIValidator validator;

    private MockHttpServletRequest servletRequest;

    @Mock
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new PostURIValidator();
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
    public void validateMatchingUrl() throws Exception {
        servletRequest.setRequestURI("/test/upload");
        when(uploadStorageService.getUploadURI()).thenReturn("/test/upload");

        try {
            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }

        //No Exception is thrown
    }

    @Test
    public void validateInvalidUrl() throws Exception {
        Throwable exception =
                assertThrows(PostOnInvalidRequestURIException.class, () -> {

                            servletRequest.setRequestURI("/test/upload/12");
                            when(uploadStorageService.getUploadURI()).thenReturn("/test/upload");

                            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
                        });
        //Expect PostOnInvalidRequestURIException
    }

    @Test
    public void validateMatchingRegexUrl() throws Exception {
        servletRequest.setRequestURI("/users/1234/files/upload");
        when(uploadStorageService.getUploadURI()).thenReturn("/users/[0-9]+/files/upload");

        try {
            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }

        //No Exception is thrown
    }

    @Test
    public void validateInvalidRegexUrl() throws Exception {
        Throwable exception =
                assertThrows(PostOnInvalidRequestURIException.class, () -> {

                            servletRequest.setRequestURI("/users/abc123/files/upload");
                            when(uploadStorageService.getUploadURI()).thenReturn("/users/[0-9]+/files/upload");

                            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
                        });
        //Expect PostOnInvalidRequestURIException
    }

    @Test
    public void validateInvalidRegexUrlPatchUrl() throws Exception {
        Throwable exception =
                assertThrows(PostOnInvalidRequestURIException.class, () -> {

                            servletRequest.setRequestURI("/users/1234/files/upload/7669c72a-3f2a-451f-a3b9-9210e7a4c02f");
                            when(uploadStorageService.getUploadURI()).thenReturn("/users/[0-9]+/files/upload");

                            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
                        });
        //Expect PostOnInvalidRequestURIException
    }
}