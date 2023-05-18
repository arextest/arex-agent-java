package io.arex.inst.httpservlet.converter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV3;
import io.arex.inst.httpservlet.adapter.impl.ServletAdapterImplV5;
import io.arex.inst.httpservlet.converter.impl.DefaultHttpMessageConverter;
import io.arex.inst.httpservlet.converter.impl.JsonHttpMessageConverter;
import io.arex.inst.httpservlet.converter.impl.XmlHttpMessageConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

public class HttpMessageConvertFactoryTest {
    ServletAdapterImplV3 adapterV3 = ServletAdapterImplV3.getInstance();
    ServletAdapterImplV5 adapterV5 = ServletAdapterImplV5.getInstance();
    HttpServletRequest mockJsonRequest = Mockito.mock(HttpServletRequest.class);
    jakarta.servlet.http.HttpServletRequest mockXmlRequest = Mockito.mock(jakarta.servlet.http.HttpServletRequest.class);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getSupportedConverter() {
        when(mockJsonRequest.getContentType()).thenReturn("application/json");
        when(mockXmlRequest.getContentType()).thenReturn("application/xml");
        assertInstanceOf(JsonHttpMessageConverter.class, HttpMessageConvertFactory.getSupportedConverter(
            mockJsonRequest, adapterV3));
        assertInstanceOf(XmlHttpMessageConverter.class, HttpMessageConvertFactory.getSupportedConverter(mockXmlRequest,
            adapterV5));
    }

    @Test
    void getDefaultConverter() {
        when(mockJsonRequest.getContentType()).thenReturn("application/jn");
        when(mockXmlRequest.getContentType()).thenReturn("application/jn");

        assertInstanceOf(DefaultHttpMessageConverter.class, HttpMessageConvertFactory.getSupportedConverter(null,
            adapterV3));

        assertInstanceOf(DefaultHttpMessageConverter.class, HttpMessageConvertFactory.getSupportedConverter(mockJsonRequest,
            null));

        assertInstanceOf(DefaultHttpMessageConverter.class, HttpMessageConvertFactory.getSupportedConverter(mockJsonRequest,
            adapterV3));
        assertInstanceOf(DefaultHttpMessageConverter.class, HttpMessageConvertFactory.getSupportedConverter(mockXmlRequest,
            adapterV5));
    }

}
