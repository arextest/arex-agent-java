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

public class ApplicationJsonBodyConvertTest {

    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    ServletAdapterImplV3 instance3 = ServletAdapterImplV3.getInstance();
    ServletAdapterImplV5 instance5 = ServletAdapterImplV5.getInstance();
    jakarta.servlet.http.HttpServletRequest mockRequest5 = Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);


    static ApplicationJsonBodyConvert applicationJsonBodyConvert = null;



    @BeforeEach
    void setUp() {
        applicationJsonBodyConvert = new ApplicationJsonBodyConvert();

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void match() {
        when(mockRequest.getContentType()).thenReturn("application/json");
        when(mockRequest5.getContentType()).thenReturn("application/json");
        assertEquals(true, applicationJsonBodyConvert.match(mockRequest,instance3));
        assertEquals(true, applicationJsonBodyConvert.match(mockRequest5,instance5));

    }

    @Test
    void getBody() {
        assertEquals(0, applicationJsonBodyConvert.getBody(instance3.wrapRequest(mockRequest),instance3).length());
        assertEquals(0, applicationJsonBodyConvert.getBody(instance5.wrapRequest(mockRequest5),instance5).length());
    }

}
