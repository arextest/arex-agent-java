package io.arex.inst.httpservlet.convert.impl;

import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

public class ApplicationXmlBodyConvertTest {

    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    ServletAdapterImplV3 instance3 = ServletAdapterImplV3.getInstance();
    ServletAdapterImplV5 instance5 = ServletAdapterImplV5.getInstance();
    jakarta.servlet.http.HttpServletRequest mockRequest5 = Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);


    static ApplicationXmlBodyConvert applicationXmlBodyConvert = null;



    @BeforeEach
    void setUp() {
        applicationXmlBodyConvert = new ApplicationXmlBodyConvert();

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void match() {
        when(mockRequest.getContentType()).thenReturn("application/xml");
        when(mockRequest5.getContentType()).thenReturn("application/xml");
        assertEquals(true, applicationXmlBodyConvert.match(mockRequest,instance3));
        assertEquals(true, applicationXmlBodyConvert.match(mockRequest5,instance5));

    }

    @Test
    void getBody() {
        assertNull(applicationXmlBodyConvert.getBody(instance3.wrapRequest(mockRequest),instance3));
        assertNull(applicationXmlBodyConvert.getBody(instance5.wrapRequest(mockRequest5),instance5));
    }
}
