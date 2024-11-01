package com.ejemplos.tus.server.termination;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.ejemplos.tus.server.upload.UploadId;
import com.ejemplos.tus.server.upload.UploadInfo;
import com.ejemplos.tus.server.upload.UploadStorageService;
import com.ejemplos.tus.server.util.TusServletRequest;
import com.ejemplos.tus.server.util.TusServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ejemplos.tus.server.HttpMethod;

@ExtendWith(MockitoExtension.class)
class TerminationDeleteRequestHandlerTest {

    private TerminationDeleteRequestHandler handler;

    private MockHttpServletRequest servletRequest;

    private MockHttpServletResponse servletResponse;

    @Mock
    private UploadStorageService uploadStorageService;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
        handler = new TerminationDeleteRequestHandler();
    }

    @Test
    void supports() {
        assertThat(handler.supports(HttpMethod.GET), is(false));
        assertThat(handler.supports(HttpMethod.POST), is(false));
        assertThat(handler.supports(HttpMethod.PUT), is(false));
        assertThat(handler.supports(HttpMethod.DELETE), is(true));
        assertThat(handler.supports(HttpMethod.HEAD), is(false));
        assertThat(handler.supports(HttpMethod.OPTIONS), is(false));
        assertThat(handler.supports(HttpMethod.PATCH), is(false));
        assertThat(handler.supports(null), is(false));
    }

    @Test
    void testWithNotExistingUpload() throws Exception {
        when(uploadStorageService.getUploadInfo(nullable(String.class), nullable(String.class))).thenReturn(null);

        handler.process(HttpMethod.DELETE, new TusServletRequest(servletRequest),
                new TusServletResponse(servletResponse), uploadStorageService, null);

        verify(uploadStorageService, never()).terminateUpload(any(UploadInfo.class));
        assertThat(servletResponse.getStatus(), is(HttpServletResponse.SC_NO_CONTENT));
    }

    @Test
    void testWithExistingUpload() throws Exception {
        final UploadId id = new UploadId(UUID.randomUUID());

        UploadInfo info = new UploadInfo();
        info.setId(id);
        info.setOffset(2L);
        info.setLength(10L);
        when(uploadStorageService.getUploadInfo(nullable(String.class), nullable(String.class))).thenReturn(info);

        handler.process(HttpMethod.DELETE, new TusServletRequest(servletRequest),
                new TusServletResponse(servletResponse), uploadStorageService, null);

        verify(uploadStorageService, times(1)).terminateUpload(info);
        assertThat(servletResponse.getStatus(), is(HttpServletResponse.SC_NO_CONTENT));
    }

}