package com.ejemplos.tus.server.creation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.util.UUID;

import com.ejemplos.tus.server.exception.InvalidUploadLengthException;
import com.ejemplos.tus.server.exception.MaxUploadLengthExceededException;
import com.ejemplos.tus.server.exception.PostOnInvalidRequestURIException;
import com.ejemplos.tus.server.upload.UploadId;
import com.ejemplos.tus.server.upload.UploadInfo;
import jakarta.servlet.http.HttpServletResponse;

import com.ejemplos.tus.server.AbstractTusExtensionIntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;

class ITCreationExtension extends AbstractTusExtensionIntegrationTest {

    private static final String UPLOAD_URI = "/test/upload";

    // It's important to return relative UPLOAD URLs in the Location header in order
    // to support HTTPS proxies
    // that sit in front of the web app
    private static final String UPLOAD_URL = UPLOAD_URI + "/";

    private UploadId id;

    @BeforeEach
    public void setUp() throws Exception {
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
        tusFeature = new CreationExtension();
        uploadInfo = null;

        id = new UploadId(UUID.randomUUID());
        servletRequest.setRequestURI(UPLOAD_URI);
        reset(uploadStorageService);

        lenient().when(uploadStorageService.getUploadURI()).thenReturn(UPLOAD_URI);
        lenient().when(uploadStorageService.create(ArgumentMatchers.any(UploadInfo.class), nullable(String.class)))
                .then(
                        new Answer<UploadInfo>() {
                            @Override
                            public UploadInfo answer(InvocationOnMock invocation) throws Throwable {
                                UploadInfo upload = invocation.getArgument(0);
                                upload.setId(id);

                                when(uploadStorageService.getUploadInfo(UPLOAD_URL + id.toString(),
                                        (String) invocation.getArgument(1))).thenReturn(upload);
                                return upload;
                            }
                        });
    }

    @Test
    void testOptions() throws Exception {
        setRequestHeaders();

        executeCall(HttpMethod.OPTIONS, false);

        // If the Server supports this extension, it MUST add creation to the
        // Tus-Extension header.
        // If the Server supports deferring length, it MUST add creation-defer-length to
        // the Tus-Extension header.
        assertResponseHeader(HttpHeader.TUS_EXTENSION, "creation", "creation-defer-length");
    }

    @Test
    void testPostWithLength() throws Exception {
        // Create upload
        servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 9);

        executeCall(HttpMethod.POST, false);

        verify(uploadStorageService, times(1)).create(notNull(UploadInfo.class),
                nullable(String.class));
        assertResponseHeader(HttpHeader.LOCATION, UPLOAD_URL + id.toString());
        assertResponseStatus(HttpServletResponse.SC_CREATED);

        // Check data with head request
        servletRequest.setRequestURI(UPLOAD_URL + id.toString());
        servletResponse = new MockHttpServletResponse();
        executeCall(HttpMethod.HEAD, false);

        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_METADATA), is(nullValue()));
        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_DEFER_LENGTH), is(nullValue()));

        // Test Patch request
        servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 9);
        servletRequest.setRequestURI(UPLOAD_URL + id.toString());
        servletResponse = new MockHttpServletResponse();
        executeCall(HttpMethod.PATCH, false);
    }

    @Test
    void testPostWithDeferredLength() throws Exception {
        // Create upload
        servletRequest.addHeader(HttpHeader.UPLOAD_DEFER_LENGTH, 1);

        executeCall(HttpMethod.POST, false);

        verify(uploadStorageService, times(1)).create(notNull(UploadInfo.class),
                nullable(String.class));
        assertResponseHeader(HttpHeader.LOCATION, UPLOAD_URL + id.toString());
        assertResponseStatus(HttpServletResponse.SC_CREATED);

        // Check data with head request
        servletRequest.setRequestURI(UPLOAD_URL + id.toString());
        servletResponse = new MockHttpServletResponse();
        executeCall(HttpMethod.HEAD, false);

        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_METADATA), is(nullValue()));
        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_DEFER_LENGTH), is("1"));

        // Test Patch request
        servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 9);
        servletRequest.setRequestURI(UPLOAD_URL + id.toString());
        servletResponse = new MockHttpServletResponse();
        executeCall(HttpMethod.PATCH, false);

        // Re-check head request
        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI(UPLOAD_URL + id.toString());
        servletResponse = new MockHttpServletResponse();
        executeCall(HttpMethod.HEAD, false);

        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_METADATA), is(nullValue()));
        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_DEFER_LENGTH), is(nullValue()));
    }

    @Test
    void testPostWithoutLength() throws Exception {
        // Create upload without any length header
        assertThrows(InvalidUploadLengthException.class, () -> {

            executeCall(HttpMethod.POST, false);
        });
    }

    @Test
    void testPostWithMetadata() throws Exception {
        // Create upload
        servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 9);
        servletRequest.addHeader(HttpHeader.UPLOAD_METADATA, "encoded metadata");

        executeCall(HttpMethod.POST, false);

        verify(uploadStorageService, times(1)).create(notNull(UploadInfo.class),
                nullable(String.class));
        assertResponseHeader(HttpHeader.LOCATION, UPLOAD_URL + id.toString());
        assertResponseStatus(HttpServletResponse.SC_CREATED);

        // Check data with head request
        servletRequest.setRequestURI(UPLOAD_URL + id.toString());
        servletResponse = new MockHttpServletResponse();
        executeCall(HttpMethod.HEAD, false);

        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_METADATA), is("encoded metadata"));
        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_DEFER_LENGTH), is(nullValue()));
    }

    @Test
    void testPostWithAllowedMaxSize() throws Exception {
        when(uploadStorageService.getMaxUploadSize()).thenReturn(100L);

        // Create upload
        servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 90);
        executeCall(HttpMethod.POST, false);

        verify(uploadStorageService, times(1)).create(notNull(UploadInfo.class),
                nullable(String.class));
        assertResponseHeader(HttpHeader.LOCATION, UPLOAD_URL + id.toString());
        assertResponseStatus(HttpServletResponse.SC_CREATED);

        // Check data with head request
        servletRequest.setRequestURI(UPLOAD_URL + id.toString());
        servletResponse = new MockHttpServletResponse();
        executeCall(HttpMethod.HEAD, false);

        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_METADATA), is(nullValue()));
        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_DEFER_LENGTH), is(nullValue()));
    }

    @Test
    void testPostWithExceededMaxSize() {
        assertThrows(MaxUploadLengthExceededException.class, () -> {

            when(uploadStorageService.getMaxUploadSize()).thenReturn(100L);

            // Create upload
            servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 110);
            executeCall(HttpMethod.POST, false);
        });
    }

    @Test
    void testPostOnInvalidUrl() {
        // Create upload
        assertThrows(PostOnInvalidRequestURIException.class, () -> {
            servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 9);
            servletRequest.setRequestURI(UPLOAD_URL + id.toString());

            executeCall(HttpMethod.POST, false);
        });
    }

    @Test
    void testPostWithValidRegexURI() throws Exception {
        reset(uploadStorageService);
        when(uploadStorageService.getUploadURI()).thenReturn("/submission/([a-z0-9]+)/files/upload");
        when(uploadStorageService.create(ArgumentMatchers.any(UploadInfo.class), nullable(String.class))).then(
                new Answer<UploadInfo>() {
                    @Override
                    public UploadInfo answer(InvocationOnMock invocation) throws Throwable {
                        UploadInfo upload = invocation.getArgument(0);
                        upload.setId(id);

                        when(uploadStorageService.getUploadInfo("/submission/0ae5f8vv4s8c/files/upload/"
                                + id.toString(), (String) invocation.getArgument(1))).thenReturn(upload);
                        return upload;
                    }
                });

        // Create upload
        servletRequest.setRequestURI("/submission/0ae5f8vv4s8c/files/upload");
        servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 9);
        servletRequest.addHeader(HttpHeader.UPLOAD_METADATA, "submission metadata");

        executeCall(HttpMethod.POST, false);

        verify(uploadStorageService, times(1)).create(notNull(UploadInfo.class),
                nullable(String.class));
        assertResponseHeader(HttpHeader.LOCATION, "/submission/0ae5f8vv4s8c/files/upload/" + id.toString());
        assertResponseStatus(HttpServletResponse.SC_CREATED);

        // Check data with head request
        servletRequest.setRequestURI("/submission/0ae5f8vv4s8c/files/upload/" + id.toString());
        servletResponse = new MockHttpServletResponse();
        executeCall(HttpMethod.HEAD, false);

        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_METADATA), is("submission metadata"));
        assertThat(servletResponse.getHeader(HttpHeader.UPLOAD_DEFER_LENGTH), is(nullValue()));
    }

    @Test
    void testPostWithInvalidRegexURI() {
        assertThrows(PostOnInvalidRequestURIException.class, () -> {
            reset(uploadStorageService);
            when(uploadStorageService.getUploadURI()).thenReturn("/submission/([a-z0-9]+)/files/upload");
            lenient().when(uploadStorageService.create(ArgumentMatchers.any(UploadInfo.class), nullable(String.class)))
                    .then(
                            new Answer<UploadInfo>() {
                                @Override
                                public UploadInfo answer(InvocationOnMock invocation) throws Throwable {
                                    UploadInfo upload = invocation.getArgument(0);
                                    upload.setId(id);

                                    when(uploadStorageService.getUploadInfo("/submission/0ae5f8vv4s8c/files/upload/"
                                            + id.toString(), (String) invocation.getArgument(1))).thenReturn(upload);
                                    return upload;
                                }
                            });

            // Create upload
            servletRequest.setRequestURI("/submission/a+b/files/upload");
            servletRequest.addHeader(HttpHeader.UPLOAD_LENGTH, 9);
            servletRequest.addHeader(HttpHeader.UPLOAD_METADATA, "submission metadata");

            executeCall(HttpMethod.POST, false);
        });
    }
}