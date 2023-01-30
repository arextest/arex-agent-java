package io.arex.inst.httpservlet.adapter.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.arex.inst.httpservlet.wrapper.CachedBodyRequestWrapperV3;
import io.arex.inst.httpservlet.wrapper.CachedBodyResponseWrapperV3;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.context.request.NativeWebRequest;

class ServletAdapterImplV3Test {
    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
    ServletAdapterImplV3 instance = ServletAdapterImplV3.getInstance();
    NativeWebRequest nativeWebRequest = Mockito.mock(NativeWebRequest.class);
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
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
        assertInstanceOf(CachedBodyRequestWrapperV3.class, instance.wrapRequest(mockRequest));
    }

    @Test
    void wrapResponse() {
        assertInstanceOf(CachedBodyResponseWrapperV3.class, instance.wrapResponse(mockResponse));
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
    void copyBodyToResponse() throws IOException {
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
        when(mockRequest.getRequestURI()).thenReturn("/commutity/httpClientTest/okHttp");
        assertEquals("/commutity/httpClientTest/okHttp", instance.getFullUrl(mockRequest));

        when(mockRequest.getQueryString()).thenReturn("k1=v1&k2=v2");
        assertEquals("/commutity/httpClientTest/okHttp?k1=v1&k2=v2", instance.getFullUrl(mockRequest));
    }

    @Test
    void getRequestURI() {
        when(mockRequest.getRequestURI()).thenReturn("/commutity/httpClientTest/okHttp");
        assertEquals("/commutity/httpClientTest/okHttp", instance.getRequestURI(mockRequest));
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

    @Test
    void getRequestBytes() {
        assertEquals(0, instance.getRequestBytes(instance.wrapRequest(mockRequest)).length);
    }

    @Test
    void getResponseBytes() {
        assertEquals(0, instance.getResponseBytes(instance.wrapResponse(mockResponse)).length);
    }
}