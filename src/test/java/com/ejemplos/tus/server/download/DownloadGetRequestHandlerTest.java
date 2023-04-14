package com.ejemplos.tus.server.download;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.UUID;

import com.ejemplos.tus.server.exception.InvalidContentTypeException;
import jakarta.servlet.http.HttpServletResponse;

import com.ejemplos.tus.server.exception.UploadInProgressException;
import com.ejemplos.tus.server.util.TusServletRequest;
import com.ejemplos.tus.server.util.TusServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.upload.UploadId;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;

@ExtendWith(MockitoExtension.class)
public class DownloadGetRequestHandlerTest {

    private DownloadGetRequestHandler handler;

    private MockHttpServletRequest servletRequest;

    private MockHttpServletResponse servletResponse;

    @Mock
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
        handler = new DownloadGetRequestHandler();
    }

    @Test
    public void supports() throws Exception {
        assertThat(handler.supports(HttpMethod.GET), is(true));
        assertThat(handler.supports(HttpMethod.POST), is(false));
        assertThat(handler.supports(HttpMethod.PUT), is(false));
        assertThat(handler.supports(HttpMethod.DELETE), is(false));
        assertThat(handler.supports(HttpMethod.HEAD), is(false));
        assertThat(handler.supports(HttpMethod.OPTIONS), is(false));
        assertThat(handler.supports(HttpMethod.PATCH), is(false));
        assertThat(handler.supports(null), is(false));
    }

    @Test
    public void testWithCompletedUploadWithMetadata() throws Exception {
        final UploadId id = new UploadId(UUID.randomUUID());

        UploadInfo info = new UploadInfo();
        info.setId(id);
        info.setOffset(10L);
        info.setLength(10L);
        info.setEncodedMetadata("name dGVzdC5qcGc=,type aW1hZ2UvanBlZw==");
        when(uploadStorageService.getUploadInfo(nullable(String.class), nullable(String.class))).thenReturn(info);

        handler.process(HttpMethod.GET, new TusServletRequest(servletRequest),
                new TusServletResponse(servletResponse), uploadStorageService, null);

        verify(uploadStorageService, times(1))
                .copyUploadTo(any(UploadInfo.class), any(OutputStream.class));
        assertThat(servletResponse.getStatus(), is(HttpServletResponse.SC_OK));
        assertThat(servletResponse.getHeader(HttpHeader.CONTENT_LENGTH), is("10"));
        assertThat(servletResponse.getHeader(HttpHeader.CONTENT_DISPOSITION),
                is("attachment; filename=\"test.jpg\"; filename*=UTF-8''test.jpg"));
        assertThat(servletResponse.getHeader(HttpHeader.CONTENT_TYPE),
                is("image/jpeg"));
        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_METADATA),
                is("name dGVzdC5qcGc=,type aW1hZ2UvanBlZw=="));

        info.setEncodedMetadata("name TmHDr3ZlIGZpbGUudHh0,type dGV4dC9wbGFpbg==");
        handler.process(HttpMethod.GET, new TusServletRequest(servletRequest),
                new TusServletResponse(servletResponse), uploadStorageService, null);
        assertThat(servletResponse.getHeader(HttpHeader.CONTENT_DISPOSITION),
                is("attachment; filename=\"Naïve file.txt\"; filename*=UTF-8''Na%C3%AFve%20file.txt"));
    }

    @Test
    public void testWithCompletedUploadWithoutMetadata() throws Exception {
        final UploadId id = new UploadId(UUID.randomUUID());

        UploadInfo info = new UploadInfo();
        info.setId(id);
        info.setOffset(10L);
        info.setLength(10L);
        when(uploadStorageService.getUploadInfo(nullable(String.class), nullable(String.class))).thenReturn(info);

        handler.process(HttpMethod.GET, new TusServletRequest(servletRequest),
                new TusServletResponse(servletResponse), uploadStorageService, null);

        verify(uploadStorageService, times(1))
                .copyUploadTo(any(UploadInfo.class), any(OutputStream.class));
        assertThat(servletResponse.getStatus(), is(HttpServletResponse.SC_OK));
        assertThat(servletResponse.getHeader(HttpHeader.CONTENT_LENGTH), is("10"));
        assertThat(servletResponse.getHeader(HttpHeader.CONTENT_DISPOSITION),
                is("attachment; filename=\"" + id.toString() + "\"; filename*=UTF-8''" + id.toString()));
        assertThat(servletResponse.getHeader(HttpHeader.CONTENT_TYPE), is("application/octet-stream"));
    }

    @Test
    public void testWithInProgressUpload() throws Exception {
        Throwable exception =
                assertThrows(UploadInProgressException.class, () -> {

                    final UploadId id = new UploadId(UUID.randomUUID());

                    UploadInfo info = new UploadInfo();
                    info.setId(id);
                    info.setOffset(8L);
                    info.setLength(10L);
                    info.setEncodedMetadata("name dGVzdC5qcGc=,type aW1hZ2UvanBlZw==");
                    when(uploadStorageService.getUploadInfo(nullable(String.class), nullable(String.class))).thenReturn(info);

                    handler.process(HttpMethod.GET, new TusServletRequest(servletRequest),
                            new TusServletResponse(servletResponse), uploadStorageService, null);
                });
    }

    @Test
    public void testWithUnknownUpload() throws Exception {
        Throwable exception =
                assertThrows(UploadInProgressException.class, () -> {

                    when(uploadStorageService.getUploadInfo(nullable(String.class), nullable(String.class))).thenReturn(null);

                    handler.process(HttpMethod.GET, new TusServletRequest(servletRequest),
                            new TusServletResponse(servletResponse), uploadStorageService, null);

                    verify(uploadStorageService, never()).copyUploadTo(any(UploadInfo.class), any(OutputStream.class));
                    assertThat(servletResponse.getStatus(), is(HttpServletResponse.SC_NO_CONTENT));
                });
    }

}