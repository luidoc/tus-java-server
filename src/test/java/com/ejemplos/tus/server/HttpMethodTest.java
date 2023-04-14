package com.ejemplos.tus.server;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

public class HttpMethodTest {

    @Test
    public void forName() throws Exception {
        assertEquals(HttpMethod.DELETE, HttpMethod.forName("delete"));
        assertEquals(HttpMethod.GET, HttpMethod.forName("get"));
        assertEquals(HttpMethod.HEAD, HttpMethod.forName("head"));
        assertEquals(HttpMethod.PATCH, HttpMethod.forName("patch"));
        assertEquals(HttpMethod.POST, HttpMethod.forName("post"));
        assertEquals(HttpMethod.PUT, HttpMethod.forName("put"));
        assertEquals(HttpMethod.OPTIONS, HttpMethod.forName("options"));
        assertNull(HttpMethod.forName("test"));
    }

    @Test
    public void getMethodNormal() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("patch");

        assertEquals(HttpMethod.PATCH,
                HttpMethod.getMethodIfSupported(servletRequest, EnumSet.allOf(HttpMethod.class)));
    }

    @Test
    public void getMethodOverridden() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("post");
        servletRequest.addHeader(HttpHeader.METHOD_OVERRIDE, "patch");

        assertEquals(HttpMethod.PATCH,
                HttpMethod.getMethodIfSupported(servletRequest, EnumSet.allOf(HttpMethod.class)));
    }

    @Test
    public void getMethodOverriddenDoesNotExist() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("post");
        servletRequest.addHeader(HttpHeader.METHOD_OVERRIDE, "test");

        assertEquals(HttpMethod.POST,
                HttpMethod.getMethodIfSupported(servletRequest, EnumSet.allOf(HttpMethod.class)));
    }

    @Test
    public void getMethodNull() {
        Throwable exception =
                assertThrows(NullPointerException.class, () -> {
                    HttpMethod.getMethodIfSupported(null, EnumSet.allOf(HttpMethod.class));
                });
    }

    @Test
    public void getMethodNotSupported() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("put");

        assertNull(HttpMethod.getMethodIfSupported(servletRequest, EnumSet.noneOf(HttpMethod.class)));
    }

    @Test
    public void getMethodRequestNotExists() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("test");

        assertNull(HttpMethod.getMethodIfSupported(servletRequest, EnumSet.noneOf(HttpMethod.class)));
    }
}