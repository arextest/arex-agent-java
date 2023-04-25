package io.arex.inst.httpservlet.convert.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApplicationJsonBodyConvertTest {

    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);

    ServletAdapterImplV3 instance3 = ServletAdapterImplV3.getInstance();
    ServletAdapterImplV5 instance5 = ServletAdapterImplV5.getInstance();
    jakarta.servlet.http.HttpServletRequest mockRequest5 = Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);
    jakarta.servlet.http.HttpServletResponse mockResponse5 = Mockito.mock(jakarta.servlet.http.HttpServletResponse.class);


    static ApplicationJsonBodyConverter applicationJsonBodyConvert = null;



    @BeforeEach
    void setUp() {
        applicationJsonBodyConvert = new ApplicationJsonBodyConverter();

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void match() {
        when(mockRequest.getContentType()).thenReturn("application/json");
        when(mockRequest5.getContentType()).thenReturn("application/json");
        assertEquals(true, applicationJsonBodyConvert.match(mockRequest,mockResponse,instance3));
        assertEquals(true, applicationJsonBodyConvert.match(mockRequest5,mockResponse5,instance5));
    }


    @Test
    void notMatch1() {
        when(mockRequest.getContentType()).thenReturn("application/jn");
        when(mockRequest5.getContentType()).thenReturn("application/n");
        assertEquals(false, applicationJsonBodyConvert.match(mockRequest,mockResponse,instance3));
        assertEquals(false, applicationJsonBodyConvert.match(mockRequest5,mockResponse5,instance5));
    }


    @Test
    void notMatch2() {
        assertEquals(false, applicationJsonBodyConvert.match(null,mockResponse,instance3));
        assertEquals(false, applicationJsonBodyConvert.match(null,mockResponse5,instance5));
    }


    @Test
    void get0Request() {
        assertEquals(0, applicationJsonBodyConvert.getRequest(null,instance3).length);
        assertEquals(0, applicationJsonBodyConvert.getRequest(null,instance5).length);
    }

    @Test
    void getRequest() {
        assertEquals(0, applicationJsonBodyConvert.getRequest(instance3.wrapRequest(mockRequest),instance3).length);
        assertEquals(0, applicationJsonBodyConvert.getRequest(instance5.wrapRequest(mockRequest5),instance5).length);
    }


    @Test
    void getResponse() {
        assertEquals(0, applicationJsonBodyConvert.getResponse(instance3.wrapResponse(mockResponse),instance3).length);
        assertEquals(0, applicationJsonBodyConvert.getResponse(instance5.wrapResponse(mockResponse5),instance5).length);
    }

}
