package com.ejemplos.tus.server.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import com.ejemplos.tus.server.upload.UploadInfo;
import jakarta.servlet.http.HttpServletResponse;

import com.ejemplos.tus.server.AbstractTusExtensionIntegrationTest;
import com.ejemplos.tus.server.exception.InvalidContentLengthException;
import com.ejemplos.tus.server.exception.InvalidContentTypeException;
import com.ejemplos.tus.server.exception.InvalidTusResumableException;
import com.ejemplos.tus.server.exception.UnsupportedMethodException;
import com.ejemplos.tus.server.exception.UploadNotFoundException;
import com.ejemplos.tus.server.exception.UploadOffsetMismatchException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import org.springframework.test.context.event.annotation.BeforeTestClass;

public class ITCoreProtocol extends AbstractTusExtensionIntegrationTest {

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
        tusFeature = new CoreProtocol();
        uploadInfo = null;
    }

    @Test
    public void getName() throws Exception {
        assertThat(tusFeature.getName(), is("core"));
    }

    @Test
    public void testUnsupportedHttpMethod() throws Exception {
        prepareUploadInfo(2L, 10L);
        setRequestHeaders(HttpHeader.TUS_RESUMABLE);
        Throwable exception =
                assertThrows(UnsupportedMethodException.class, () -> {

                    executeCall(HttpMethod.forName("TEST"), false);
                });
    }

    @Test
    public void testHeadWithLength() throws Exception {
        prepareUploadInfo(2L, 10L);
        setRequestHeaders(HttpHeader.TUS_RESUMABLE);

        executeCall(HttpMethod.HEAD, false);

        assertResponseHeader(HttpHeader.TUS_RESUMABLE, "1.0.0");
        assertResponseHeader(HttpHeader.UPLOAD_OFFSET, "2");
        assertResponseHeader(HttpHeader.UPLOAD_LENGTH, "10");
        assertResponseHeader(HttpHeader.CACHE_CONTROL, "no-store");
        assertResponseStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testHeadWithoutLength() throws Exception {
        prepareUploadInfo(2L, null);
        setRequestHeaders(HttpHeader.TUS_RESUMABLE);

        executeCall(HttpMethod.HEAD, false);

        assertResponseHeader(HttpHeader.TUS_RESUMABLE, "1.0.0");
        assertResponseHeader(HttpHeader.UPLOAD_OFFSET, "2");
        assertResponseHeader(HttpHeader.UPLOAD_LENGTH, (String) null);
        assertResponseHeader(HttpHeader.CACHE_CONTROL, "no-store");
        assertResponseStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testHeadNotFound() throws Exception {
        //We don't prepare an upload info
        setRequestHeaders(HttpHeader.TUS_RESUMABLE);
        Throwable exception =
                assertThrows(UploadNotFoundException.class, () -> {
                    executeCall(HttpMethod.HEAD, false);
                });
    }

    @Test
    public void testHeadInvalidVersion() throws Exception {
        setRequestHeaders();
        Throwable exception =
                assertThrows(InvalidTusResumableException.class, () -> {
                    prepareUploadInfo(2L, null);
                    servletRequest.addHeader(HttpHeader.TUS_RESUMABLE, "2.0.0");

                    executeCall(HttpMethod.HEAD, false);
                });
    }

    @Test
    public void testPatchSuccess() throws Exception {
        prepareUploadInfo(2L, 10L);
        setRequestHeaders(HttpHeader.TUS_RESUMABLE, HttpHeader.CONTENT_TYPE, HttpHeader.UPLOAD_OFFSET,
                HttpHeader.CONTENT_LENGTH);

        executeCall(HttpMethod.PATCH, false);

        verify(uploadStorageService, times(1))
                .append(any(UploadInfo.class), any(InputStream.class));

        assertResponseHeader(HttpHeader.TUS_RESUMABLE, "1.0.0");
        assertResponseHeader(HttpHeader.UPLOAD_OFFSET, "2");
        assertResponseHeader(HttpHeader.UPLOAD_LENGTH, (String) null);
        assertResponseHeader(HttpHeader.CACHE_CONTROL, (String) null);
        assertResponseStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testPatchInvalidContentType() throws Exception {
        Throwable exception =
                assertThrows(InvalidContentTypeException.class, () -> {
                    prepareUploadInfo(2L, 10L);
                    setRequestHeaders(HttpHeader.TUS_RESUMABLE, HttpHeader.UPLOAD_OFFSET, HttpHeader.CONTENT_LENGTH);

                    executeCall(HttpMethod.PATCH, false);
                });
    }

    @Test
    public void testPatchInvalidUploadOffset() throws Exception {
        Throwable exception =
                assertThrows(UploadOffsetMismatchException.class, () -> {

                    prepareUploadInfo(2L, 10L);
                    setRequestHeaders(HttpHeader.TUS_RESUMABLE, HttpHeader.CONTENT_TYPE, HttpHeader.CONTENT_LENGTH);
                    servletRequest.addHeader(HttpHeader.UPLOAD_OFFSET, 5);

                    executeCall(HttpMethod.PATCH, false);
                });
    }

    @Test
    public void testPatchInvalidContentLength() throws Exception {
        Throwable exception =
                assertThrows(InvalidContentLengthException.class, () -> {
                    prepareUploadInfo(2L, 10L);
                    setRequestHeaders(HttpHeader.TUS_RESUMABLE, HttpHeader.CONTENT_TYPE, HttpHeader.UPLOAD_OFFSET);
                    servletRequest.addHeader(HttpHeader.CONTENT_LENGTH, 9);

                    executeCall(HttpMethod.PATCH, false);
                });
    }

    @Test
    public void testOptionsWithMaxSize() throws Exception {
        when(uploadStorageService.getMaxUploadSize()).thenReturn(107374182400L);

        setRequestHeaders();

        executeCall(HttpMethod.OPTIONS, false);

        assertResponseHeader(HttpHeader.TUS_RESUMABLE, "1.0.0");
        assertResponseHeader(HttpHeader.TUS_VERSION, "1.0.0");
        assertResponseHeader(HttpHeader.TUS_MAX_SIZE, "107374182400");
        assertResponseHeader(HttpHeader.TUS_EXTENSION, (String) null);
        assertResponseStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testOptionsWithNoMaxSize() throws Exception {
        when(uploadStorageService.getMaxUploadSize()).thenReturn(0L);

        setRequestHeaders();

        executeCall(HttpMethod.OPTIONS, false);

        assertResponseHeader(HttpHeader.TUS_RESUMABLE, "1.0.0");
        assertResponseHeader(HttpHeader.TUS_VERSION, "1.0.0");
        assertResponseHeader(HttpHeader.TUS_MAX_SIZE, (String) null);
        assertResponseHeader(HttpHeader.TUS_EXTENSION, (String) null);
        assertResponseStatus(HttpServletResponse.SC_NO_CONTENT);
    }


    @Test
    public void testOptionsIgnoreTusResumable() throws Exception {
        when(uploadStorageService.getMaxUploadSize()).thenReturn(10L);

        setRequestHeaders();
        servletRequest.addHeader(HttpHeader.TUS_RESUMABLE, "2.0.0");

        executeCall(HttpMethod.OPTIONS, false);

        assertResponseHeader(HttpHeader.TUS_RESUMABLE, "1.0.0");
        assertResponseHeader(HttpHeader.TUS_VERSION, "1.0.0");
        assertResponseHeader(HttpHeader.TUS_MAX_SIZE, "10");
        assertResponseHeader(HttpHeader.TUS_EXTENSION, (String) null);
        assertResponseStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}