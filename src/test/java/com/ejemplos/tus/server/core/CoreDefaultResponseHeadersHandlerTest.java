package com.ejemplos.tus.server.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.ejemplos.tus.server.util.TusServletRequest;
import com.ejemplos.tus.server.util.TusServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;
import com.ejemplos.tus.server.TusFileUploadService;

class CoreDefaultResponseHeadersHandlerTest {

    private MockHttpServletRequest servletRequest;

    private MockHttpServletResponse servletResponse;

    private CoreDefaultResponseHeadersHandler handler;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
        handler = new CoreDefaultResponseHeadersHandler();
    }

    @Test
    void supports() throws Exception {
        assertThat(handler.supports(HttpMethod.GET), is(true));
        assertThat(handler.supports(HttpMethod.POST), is(true));
        assertThat(handler.supports(HttpMethod.PUT), is(true));
        assertThat(handler.supports(HttpMethod.DELETE), is(true));
        assertThat(handler.supports(HttpMethod.HEAD), is(true));
        assertThat(handler.supports(HttpMethod.OPTIONS), is(true));
        assertThat(handler.supports(HttpMethod.PATCH), is(true));
        assertThat(handler.supports(null), is(true));
    }

    @Test
    void process() throws Exception {
        handler.process(HttpMethod.PATCH, new TusServletRequest(servletRequest),
                new TusServletResponse(servletResponse), null, null);

        assertThat(servletResponse.getHeader(HttpHeader.TUS_RESUMABLE), is(TusFileUploadService.TUS_API_VERSION));
        assertThat(servletResponse.getHeader(HttpHeader.CONTENT_LENGTH), is("0"));
    }
}