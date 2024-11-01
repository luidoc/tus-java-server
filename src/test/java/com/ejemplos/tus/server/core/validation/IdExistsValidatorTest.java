package com.ejemplos.tus.server.core.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.ejemplos.tus.server.exception.UploadNotFoundException;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ejemplos.tus.server.HttpMethod;

@ExtendWith(MockitoExtension.class)
class IdExistsValidatorTest {

    private IdExistsValidator validator;

    private MockHttpServletRequest servletRequest;

    @Mock
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new IdExistsValidator();
    }

    @Test
    void validateValid() throws Exception {
        UploadInfo info = new UploadInfo();
        info.setOffset(0L);
        info.setLength(10L);
        when(uploadStorageService.getUploadInfo(nullable(String.class), nullable(String.class))).thenReturn(info);

        //When we validate the request
        try {
            validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }

        //No Exception is thrown
    }

    @Test
    void validateInvalid() {
                assertThrows(UploadNotFoundException.class, () -> {

                            when(uploadStorageService.getUploadInfo(nullable(String.class), nullable(String.class))).thenReturn(null);

                            //When we validate the request
                            validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
                        });
        //Expect a UploadNotFoundException
    }

    @Test
    void supports() {
        assertThat(validator.supports(HttpMethod.GET), is(true));
        assertThat(validator.supports(HttpMethod.POST), is(false));
        assertThat(validator.supports(HttpMethod.PUT), is(false));
        assertThat(validator.supports(HttpMethod.DELETE), is(true));
        assertThat(validator.supports(HttpMethod.HEAD), is(true));
        assertThat(validator.supports(HttpMethod.OPTIONS), is(false));
        assertThat(validator.supports(HttpMethod.PATCH), is(true));
        assertThat(validator.supports(null), is(false));
    }

}