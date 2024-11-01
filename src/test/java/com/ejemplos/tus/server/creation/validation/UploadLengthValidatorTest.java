package com.ejemplos.tus.server.creation.validation;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.exception.MaxUploadLengthExceededException;
import com.ejemplos.tus.server.upload.UploadStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UploadLengthValidatorTest {

    private UploadLengthValidator validator;

    private MockHttpServletRequest servletRequest;

    @Mock
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new UploadLengthValidator();
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

    @ParameterizedTest
    @CsvSource({
        "0, 300",
        "400, 300",
        "300, 300"
    })
    void validateUploadLength(long maxUploadSize, long uploadLength) throws Exception {
        when(uploadStorageService.getMaxUploadSize()).thenReturn(maxUploadSize);
        servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, uploadLength);

        try {
            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

    @Test
    void validateNoUploadLength() throws Exception {
        when(uploadStorageService.getMaxUploadSize()).thenReturn(300L);

        try {
            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
        } catch (Exception ex) {
            fail();
        }

        // No Exception is thrown
    }

    @Test
    void validateAboveMaxUploadLength() {
        assertThrows(MaxUploadLengthExceededException.class, () -> {

            when(uploadStorageService.getMaxUploadSize()).thenReturn(200L);
            servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 300L);

            validator.validate(HttpMethod.POST, servletRequest, uploadStorageService, null);
        });
        // Expect a MaxUploadLengthExceededException
    }
}