package io.arex.inst.httpservlet.converter.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class JsonHttpMessageConverterTest {

    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);

    ServletAdapterImplV3 instance3 = ServletAdapterImplV3.getInstance();
    ServletAdapterImplV5 instance5 = ServletAdapterImplV5.getInstance();
    jakarta.servlet.http.HttpServletRequest mockRequest5 = Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);
    jakarta.servlet.http.HttpServletResponse mockResponse5 = Mockito.mock(
        jakarta.servlet.http.HttpServletResponse.class);


    static JsonHttpMessageConverter jsonHttpMessageConverter = null;


    @BeforeEach
    void setUp() {
        jsonHttpMessageConverter = new JsonHttpMessageConverter();

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void match() {
        when(mockRequest.getContentType()).thenReturn("application/json");
        when(mockRequest5.getContentType()).thenReturn("application/json");
        assertEquals(true, jsonHttpMessageConverter.support(mockRequest, instance3));
        assertEquals(true, jsonHttpMessageConverter.support(mockRequest5, instance5));
    }


    @Test
    void notMatch1() {
        when(mockRequest.getContentType()).thenReturn("application/jn");
        when(mockRequest5.getContentType()).thenReturn("application/n");
        assertEquals(false, jsonHttpMessageConverter.support(mockRequest, instance3));
        assertEquals(false, jsonHttpMessageConverter.support(mockRequest5, instance5));
    }


    @Test
    void getRequest() {
        assertEquals(0, jsonHttpMessageConverter.getRequest(instance3.wrapRequest(mockRequest), instance3).length);
        assertEquals(0, jsonHttpMessageConverter.getRequest(instance5.wrapRequest(mockRequest5), instance5).length);
    }


    @Test
    void getResponse() {
        assertEquals(0, jsonHttpMessageConverter.getResponse(instance3.wrapResponse(mockResponse), instance3).length);
        assertEquals(0, jsonHttpMessageConverter.getResponse(instance5.wrapResponse(mockResponse5), instance5).length);
    }

}
