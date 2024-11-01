package com.ejemplos.tus.server;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class HttpMethodTest {

    @Test
    void forName() {
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
    void getMethodNormal() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("patch");

        assertEquals(HttpMethod.PATCH,
                HttpMethod.getMethodIfSupported(servletRequest, EnumSet.allOf(HttpMethod.class)));
    }

    @Test
    void getMethodOverridden() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("post");
        servletRequest.addHeader(HttpHeader.METHOD_OVERRIDE, "patch");

        assertEquals(HttpMethod.PATCH,
                HttpMethod.getMethodIfSupported(servletRequest, EnumSet.allOf(HttpMethod.class)));
    }

    @Test
    void getMethodOverriddenDoesNotExist() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("post");
        servletRequest.addHeader(HttpHeader.METHOD_OVERRIDE, "test");

        assertEquals(HttpMethod.POST,
                HttpMethod.getMethodIfSupported(servletRequest, EnumSet.allOf(HttpMethod.class)));
    }

    @Test
    void getMethodNull() {
        assertThrows(NullPointerException.class, () -> {
            HttpMethod.getMethodIfSupported(null, EnumSet.allOf(HttpMethod.class));
        });
    }

    @Test
    void getMethodNotSupported() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("put");

        assertNull(HttpMethod.getMethodIfSupported(servletRequest, EnumSet.noneOf(HttpMethod.class)));
    }

    @Test
    void getMethodRequestNotExists() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("test");

        assertNull(HttpMethod.getMethodIfSupported(servletRequest, EnumSet.noneOf(HttpMethod.class)));
    }
}