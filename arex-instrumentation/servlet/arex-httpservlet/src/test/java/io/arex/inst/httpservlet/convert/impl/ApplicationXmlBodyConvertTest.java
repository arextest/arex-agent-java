package io.arex.inst.httpservlet.convert.impl;

import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

public class ApplicationXmlBodyConvertTest {

    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);

    ServletAdapterImplV3 instance3 = ServletAdapterImplV3.getInstance();
    ServletAdapterImplV5 instance5 = ServletAdapterImplV5.getInstance();
    jakarta.servlet.http.HttpServletRequest mockRequest5 = Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);
    jakarta.servlet.http.HttpServletResponse mockResponse5 = Mockito.mock(jakarta.servlet.http.HttpServletResponse.class);


    static ApplicationXmlBodyConverter applicationXmlBodyConvert = null;



    @BeforeEach
    void setUp() {
        applicationXmlBodyConvert = new ApplicationXmlBodyConverter();

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void match() {
        when(mockRequest.getContentType()).thenReturn("application/xml");
        when(mockRequest5.getContentType()).thenReturn("application/xml");
        assertEquals(true, applicationXmlBodyConvert.match(mockRequest,mockResponse,instance3));
        assertEquals(true, applicationXmlBodyConvert.match(mockRequest5,mockResponse5,instance5));
    }


    @Test
    void notMatch1() {
        when(mockRequest.getContentType()).thenReturn("application/xl");
        when(mockRequest5.getContentType()).thenReturn("application/xl");
        assertEquals(false, applicationXmlBodyConvert.match(mockRequest,mockResponse,instance3));
        assertEquals(false, applicationXmlBodyConvert.match(mockRequest5,mockResponse5,instance5));
    }


    @Test
    void notMatch2() {
        assertEquals(false, applicationXmlBodyConvert.match(null,mockResponse,instance3));
        assertEquals(false, applicationXmlBodyConvert.match(null,mockResponse5,instance5));
    }


    @Test
    void get0Request() {
        assertEquals(0, applicationXmlBodyConvert.getRequest(null,instance3).length);
        assertEquals(0, applicationXmlBodyConvert.getRequest(null,instance5).length);
    }

    @Test
    void getRequest() {
        assertEquals(0, applicationXmlBodyConvert.getRequest(instance3.wrapRequest(mockRequest),instance3).length);
        assertEquals(0, applicationXmlBodyConvert.getRequest(instance5.wrapRequest(mockRequest5),instance5).length);
    }


    @Test
    void getResponse() {
        assertEquals(0, applicationXmlBodyConvert.getResponse(instance3.wrapResponse(mockResponse),instance3).length);
        assertEquals(0, applicationXmlBodyConvert.getResponse(instance5.wrapResponse(mockResponse5),instance5).length);
    }

}
