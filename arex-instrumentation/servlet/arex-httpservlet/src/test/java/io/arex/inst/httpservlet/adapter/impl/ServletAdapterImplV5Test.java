package io.arex.inst.httpservlet.adapter.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.inst.httpservlet.SpringUtil;
import io.arex.inst.httpservlet.wrapper.CachedBodyRequestWrapperV5;
import io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV5;
import io.arex.inst.runtime.model.ArexConstants;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.context.request.NativeWebRequest;

class ServletAdapterImplV5Test {

    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
    ServletAdapterImplV5 instance = ServletAdapterImplV5.getInstance();
    NativeWebRequest nativeWebRequest = Mockito.mock(NativeWebRequest.class);

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ReflectUtil.class);
        Mockito.mockStatic(SpringUtil.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void getInstance() {
        assertNotNull(instance);
    }

    @Test
    void getNativeRequest() {
        when(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(mockRequest);
        assertSame(mockRequest, instance.getNativeRequest(nativeWebRequest));
    }

    @Test
    void getRequestHeader() {
        when(mockRequest.getHeader("mock-header-name")).thenReturn("mock-header-value");
        String actualValue = instance.getRequestHeader(mockRequest, "mock-header-name");
        assertEquals("mock-header-value", actualValue);
    }

    @Test
    void wrapRequest() {
        assertInstanceOf(CachedBodyRequestWrapperV5.class, instance.wrapRequest(instance.wrapRequest(mockRequest)));
    }

    @Test
    void wrapResponse() {
        assertInstanceOf(CachedBodyResponseWrapperV5.class, instance.wrapResponse(instance.wrapResponse(mockResponse)));
    }

    @Test
    void setResponseHeader() {
        instance.setResponseHeader(mockResponse, "mock-header-name", "mock-header-value");
        Mockito.verify(mockResponse).setHeader("mock-header-name", "mock-header-value");
    }

    @Test
    void getStatus() {
        when(mockResponse.getStatus()).thenReturn(200);
        assertEquals(200, instance.getStatus(mockResponse));
    }

    @Test
    void isAsyncStarted() {
        when(mockRequest.isAsyncStarted()).thenReturn(true);
        assertTrue(instance.isAsyncStarted(mockRequest));
    }

    @Test
    void getAttribute() {
        when(mockRequest.getAttribute("mock-attribute-name")).thenReturn("mock-attribute-value");
        String actualValue = (String) instance.getAttribute(mockRequest, "mock-attribute-name");
        assertEquals("mock-attribute-value", actualValue);
    }

    @Test
    void setAttribute() {
        instance.setAttribute(mockRequest, "mock-attribute-name", "mock-attribute-value");
        Mockito.verify(mockRequest).setAttribute("mock-attribute-name", "mock-attribute-value");
    }

    @Test
    void removeAttribute() {
        instance.removeAttribute(mockRequest, "mock-attribute-name");
        Mockito.verify(mockRequest).removeAttribute("mock-attribute-name");
    }

    @Test
    void wrapped() {
        assertTrue(instance.wrapped(instance.wrapRequest(mockRequest), instance.wrapResponse(mockResponse)));
    }

    @Test
    void copyBodyToResponse() {
        assertDoesNotThrow(() -> instance.copyBodyToResponse(instance.wrapResponse(mockResponse)));
    }

    @Test
    void addListener() {
        when(mockRequest.getAsyncContext()).thenReturn(Mockito.mock(AsyncContext.class));
        assertDoesNotThrow(() -> instance.addListener(instance, mockRequest, mockResponse));
    }

    @Test
    void getContentType() {
        when(mockRequest.getContentType()).thenReturn("mock-content-type");
        assertEquals("mock-content-type", instance.getContentType(mockRequest));
    }

    @Test
    void getFullUrl() {
        when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("http://arextest.com/servletpath/controll/action"));
        assertEquals("http://arextest.com/servletpath/controll/action", instance.getFullUrl(mockRequest));

        when(mockRequest.getQueryString()).thenReturn("k1=v1&k2=v2");
        assertEquals("http://arextest.com/servletpath/controll/action?k1=v1&k2=v2", instance.getFullUrl(mockRequest));
    }

    @Test
    void getRequestPath() {
        when(mockRequest.getRequestURI()).thenReturn("/commutity/httpClientTest/okHttp");
        assertEquals("/commutity/httpClientTest/okHttp", instance.getRequestPath(mockRequest));

        when(mockRequest.getQueryString()).thenReturn("k1=v1&k2=v2");
        assertEquals("/commutity/httpClientTest/okHttp?k1=v1&k2=v2", instance.getRequestPath(mockRequest));
    }

    @Test
    void getRequestURI() {
        when(mockRequest.getRequestURI()).thenReturn("/commutity/httpClientTest/okHttp");
        assertEquals("/commutity/httpClientTest/okHttp", instance.getRequestURI(mockRequest));
    }

    @Test
    void getPattern() {
        when(mockRequest.getAttribute(eq("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern"))).thenReturn("/book/{id}");
        assertEquals("/book/{id}", instance.getPattern(mockRequest));

        when(mockRequest.getAttribute(eq("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern"))).thenReturn(null);
        when(mockRequest.getRequestURI()).thenReturn("/commutity/httpClientTest/okHttp");
        assertEquals("/commutity/httpClientTest/okHttp", instance.getPattern(mockRequest));

        // get pattern from request mapping in spring applicationContext
        when(SpringUtil.getPatternFromRequestMapping(any(), any())).thenReturn("/book/{id}");
        assertEquals("/book/{id}", instance.getPattern(mockRequest));

        when(SpringUtil.getPatternFromRequestMapping(any(), any())).thenReturn(null);
        when(mockRequest.getContextPath()).thenReturn("/commutity");
        assertEquals("/httpClientTest/okHttp", instance.getPattern(mockRequest));
    }

    @Test
    void getResponseHeader() {
        when(mockResponse.getHeader("mock-header-name")).thenReturn("mock-header-value");
        assertEquals("mock-header-value", instance.getResponseHeader(mockResponse, "mock-header-name"));
    }

    @Test
    void getMethod() {
        when(mockRequest.getMethod()).thenReturn("POST");
        assertEquals("POST", instance.getMethod(mockRequest));
    }

    @Test
    void getRequestHeaderNames() {
        when(mockRequest.getHeaderNames()).thenReturn(new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public String nextElement() {
                return "mock-header-name";
            }
        });

        assertEquals("mock-header-name", instance.getRequestHeaderNames(mockRequest).nextElement());
    }

    @Test
    void getResponseHeaderNames() {
        when(mockResponse.getHeaderNames()).thenReturn(Collections.singleton("mock-header-name"));

        assertEquals("mock-header-name", instance.getResponseHeaderNames(mockResponse).stream().findFirst().get());
    }

    static class MockServletInputStream extends ServletInputStream {
        private final InputStream delegate;
        public MockServletInputStream(byte[] body) {
            this.delegate = new ByteArrayInputStream(body);
        }
        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return this.delegate.read();
        }
    }

    @Test
    void getRequestBytes() throws IOException {
        HttpServletRequest requestWrapper = instance.wrapRequest(mockRequest);
        // content empty
        assertArrayEquals(new byte[0], instance.getRequestBytes(requestWrapper));


        byte[] body = "mock".getBytes();
        when(mockRequest.getInputStream()).thenReturn(new MockServletInputStream(body));
        when(mockRequest.getContentLength()).thenReturn(body.length);

        // read request body to cache
        assertArrayEquals(body, instance.getRequestBytes(requestWrapper));

        // getContentAsByteArray.length > 0
        assertArrayEquals(body, instance.getRequestBytes(requestWrapper));
    }

    @Test
    void getResponseBytes() {
        assertEquals(0, instance.getResponseBytes(instance.wrapResponse(mockResponse)).length);
    }

    @Test
    void asHttpServletRequest() {
        assertNotNull(instance.asHttpServletRequest(mockRequest));
        assertNull(instance.asHttpServletRequest(null));
    }

    @Test
    void asHttpServletResponse() {
        assertNotNull(instance.asHttpServletResponse(mockResponse));
        assertNull(instance.asHttpServletResponse(null));
    }

    @Test
    void markProcessed() {
        when(mockRequest.getAttribute(any())).thenReturn("mock");
        assertTrue(instance.markProcessed(mockRequest, "mock"));
        when(mockRequest.getAttribute(any())).thenReturn(null);
        assertFalse(instance.markProcessed(mockRequest, "mock"));
    }

    @Test
    void getQueryString() {
        when(mockRequest.getQueryString()).thenReturn("k1=v1&k2=v2");
        assertEquals("k1=v1&k2=v2", instance.getQueryString(mockRequest));
    }

    @Test
    void getServletVersion() {
        assertEquals(ArexConstants.SERVLET_V5, instance.getServletVersion());
    }
}
