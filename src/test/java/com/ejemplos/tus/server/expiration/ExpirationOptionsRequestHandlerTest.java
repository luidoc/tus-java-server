package com.ejemplos.tus.server.expiration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Arrays;

import com.ejemplos.tus.server.util.TusServletRequest;
import com.ejemplos.tus.server.util.TusServletResponse;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ejemplos.tus.server.HttpHeader;
import com.ejemplos.tus.server.HttpMethod;


public class ExpirationOptionsRequestHandlerTest {

    private ExpirationOptionsRequestHandler handler;

    private MockHttpServletRequest servletRequest;

    private MockHttpServletResponse servletResponse;

    @BeforeEach
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
        handler = new ExpirationOptionsRequestHandler();
    }

    @Test
    public void processListExtensions() throws Exception {

        handler.process(HttpMethod.OPTIONS, new TusServletRequest(servletRequest),
                new TusServletResponse(servletResponse), null, null);

        assertThat(Arrays.asList(servletResponse.getHeader(HttpHeader.TUS_EXTENSION).split(",")),
                containsInAnyOrder("expiration"));
    }

    @Test
    public void supports() throws Exception {
        MatcherAssert.assertThat(handler.supports(HttpMethod.GET), is(false));
        MatcherAssert.assertThat(handler.supports(HttpMethod.POST), is(false));
        MatcherAssert.assertThat(handler.supports(HttpMethod.PUT), is(false));
        MatcherAssert.assertThat(handler.supports(HttpMethod.DELETE), is(false));
        MatcherAssert.assertThat(handler.supports(HttpMethod.HEAD), is(false));
        MatcherAssert.assertThat(handler.supports(HttpMethod.OPTIONS), is(true));
        MatcherAssert.assertThat(handler.supports(HttpMethod.PATCH), is(false));
        MatcherAssert.assertThat(handler.supports(null), is(false));
    }

}