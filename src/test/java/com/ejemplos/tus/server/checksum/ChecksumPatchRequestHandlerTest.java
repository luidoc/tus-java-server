package com.ejemplos.tus.server.checksum;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ejemplos.tus.server.exception.ChecksumAlgorithmNotSupportedException;
import com.ejemplos.tus.server.exception.UploadChecksumMismatchException;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.TusServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChecksumPatchRequestHandlerTest {

    private ChecksumPatchRequestHandler handler;

    @Mock
    private TusServletRequest servletRequest;

    @Mock
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() throws Exception {
        handler = new ChecksumPatchRequestHandler();

        UploadInfo info = new UploadInfo();
        info.setOffset(2L);
        info.setLength(10L);
        when(uploadStorageService.getUploadInfo(nullable(String.class), nullable(String.class))).thenReturn(info);
    }

    @Test
    void supports() {
        assertThat(handler.supports(HttpMethod.GET), is(false));
        assertThat(handler.supports(HttpMethod.POST), is(false));
        assertThat(handler.supports(HttpMethod.PUT), is(false));
        assertThat(handler.supports(HttpMethod.DELETE), is(false));
        assertThat(handler.supports(HttpMethod.HEAD), is(false));
        assertThat(handler.supports(HttpMethod.OPTIONS), is(false));
        assertThat(handler.supports(HttpMethod.PATCH), is(true));
        assertThat(handler.supports(null), is(false));
    }

    @Test
    void testValidHeaderAndChecksum() throws Exception {
        when(servletRequest.getHeader(HttpHeader.UPLOAD_CHECKSUM)).thenReturn("sha1 1234567890");
        when(servletRequest.getCalculatedChecksum(ArgumentMatchers.any(ChecksumAlgorithm.class)))
                .thenReturn("1234567890");
        when(servletRequest.hasCalculatedChecksum()).thenReturn(true);

        handler.process(HttpMethod.PATCH, servletRequest, null, uploadStorageService, null);

        verify(servletRequest, times(1)).getCalculatedChecksum(any(ChecksumAlgorithm.class));
    }

    @Test
    void testValidHeaderAndInvalidChecksum(){
        assertThrows(UploadChecksumMismatchException.class, () -> {
            when(servletRequest.getHeader(HttpHeader.UPLOAD_CHECKSUM)).thenReturn("sha1 1234567890");
            when(servletRequest.getCalculatedChecksum(ArgumentMatchers.any(ChecksumAlgorithm.class)))
                    .thenReturn("0123456789");
            when(servletRequest.hasCalculatedChecksum()).thenReturn(true);

            handler.process(HttpMethod.PATCH, servletRequest, null, uploadStorageService, null);
        });
    }

    @Test
    void testNoHeader() throws Exception {
        when(servletRequest.getHeader(HttpHeader.UPLOAD_CHECKSUM)).thenReturn(null);

        handler.process(HttpMethod.PATCH, servletRequest, null, uploadStorageService, null);

        verify(servletRequest, never()).getCalculatedChecksum(any(ChecksumAlgorithm.class));
    }

    @Test
    void testInvalidHeader() {
        assertThrows(ChecksumAlgorithmNotSupportedException.class, () -> {
            when(servletRequest.getHeader(HttpHeader.UPLOAD_CHECKSUM)).thenReturn("test 1234567890");
            when(servletRequest.hasCalculatedChecksum()).thenReturn(true);

            handler.process(HttpMethod.PATCH, servletRequest, null, uploadStorageService, null);
        });
    }
}