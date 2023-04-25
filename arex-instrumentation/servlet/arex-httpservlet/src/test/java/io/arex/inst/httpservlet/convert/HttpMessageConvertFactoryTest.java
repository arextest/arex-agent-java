package io.arex.inst.httpservlet.convert;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5;
import io.arex.inst.httpservlet.convert.impl.DefaultHttpMessageConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpMessageConvertFactoryTest {

    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
    ServletAdapterImplV3 instance3 = ServletAdapterImplV3.getInstance();
    ServletAdapterImplV5 instance5 = ServletAdapterImplV5.getInstance();
    jakarta.servlet.http.HttpServletRequest mockRequest5 = Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);
    jakarta.servlet.http.HttpServletResponse mockResponse5 = Mockito.mock(jakarta.servlet.http.HttpServletResponse.class);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getOne(){
        when(mockRequest.getContentType()).thenReturn("application/json");
        when(mockRequest5.getContentType()).thenReturn("application/json");
        assertNotNull(HttpMessageConvertFactory.getSupportedConverter(mockRequest,mockResponse,instance3));
        assertNotNull(HttpMessageConvertFactory.getSupportedConverter(mockRequest5,mockResponse5,instance5));
    }

    @Test
    void getDefault(){
        when(mockRequest.getContentType()).thenReturn("application/jn");
        when(mockRequest5.getContentType()).thenReturn("application/jn");

        assertTrue(HttpMessageConvertFactory.getSupportedConverter(mockRequest,mockResponse,instance3) instanceof DefaultHttpMessageConverter);
        assertTrue(HttpMessageConvertFactory.getSupportedConverter(mockRequest5,mockResponse5,instance5) instanceof DefaultHttpMessageConverter);
    }

}
