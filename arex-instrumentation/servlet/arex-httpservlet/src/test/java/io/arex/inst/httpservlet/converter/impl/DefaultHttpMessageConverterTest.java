package io.arex.inst.httpservlet.converter.impl;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5;
import io.arex.inst.httpservlet.converter.HttpMessageConverter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultHttpMessageConverterTest {
    static HttpMessageConverter defaultHttpMessageConverter = null;

    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);

    ServletAdapterImplV3 instance3 = ServletAdapterImplV3.getInstance();
    ServletAdapterImplV5 instance5 = ServletAdapterImplV5.getInstance();
    jakarta.servlet.http.HttpServletRequest mockRequest5 = Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);
    jakarta.servlet.http.HttpServletResponse mockResponse5 = Mockito.mock(jakarta.servlet.http.HttpServletResponse.class);


    @BeforeAll
    static void setUp() {
        defaultHttpMessageConverter = DefaultHttpMessageConverter.getInstance();

    }

    @AfterAll
    static void tearDown() {
    }


    @Test
    void support() {
        assertFalse(defaultHttpMessageConverter.support(null, null));
    }

    @Test
    void getRequest() {
        assertEquals(0, defaultHttpMessageConverter.getRequest(instance3.wrapRequest(mockRequest),instance3).length);
        assertEquals(0, defaultHttpMessageConverter.getRequest(instance5.wrapRequest(mockRequest5),instance5).length);
    }


    @Test
    void getResponse() {
        assertEquals(0, defaultHttpMessageConverter.getResponse(instance3.wrapResponse(mockResponse),instance3).length);
        assertEquals(0, defaultHttpMessageConverter.getResponse(instance5.wrapResponse(mockResponse5),instance5).length);
    }
}
