package io.arex.inst.httpclient.ning;

import io.arex.inst.httpclient.common.HttpResponseWrapper;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseWrapperTest {
    private static ResponseWrapper responseWrapper = null;

    @BeforeAll
    static void setUp() {
        HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper();
        httpResponseWrapper.setContent("Test content".getBytes());
        httpResponseWrapper.setStatusLine("OK");
        httpResponseWrapper.setStatusCode(200);
        List<HttpResponseWrapper.StringTuple> list = getStringTuples();
        httpResponseWrapper.setHeaders(list);
        responseWrapper = new ResponseWrapper(httpResponseWrapper);
    }

    private static List<HttpResponseWrapper.StringTuple> getStringTuples() {
        List<HttpResponseWrapper.StringTuple> list = new ArrayList<>();
        HttpResponseWrapper.StringTuple stringTuple = new HttpResponseWrapper.StringTuple("Content-Type", "application/json");
        HttpResponseWrapper.StringTuple stringTuple1 = new HttpResponseWrapper.StringTuple("Content-Length", "100");
        HttpResponseWrapper.StringTuple cookies = new HttpResponseWrapper.StringTuple(HttpHeaders.Names.SET_COOKIE, "test1=1, test2=2");
        list.add(stringTuple);
        list.add(stringTuple1);
        list.add(cookies);
        return list;
    }

    @AfterAll
    static void tearDown() {
        responseWrapper = null;
    }

    @Test
    void getStatusCode() {
        assertEquals(200, responseWrapper.getStatusCode());
    }

    @Test
    void getStatusText() {
        assertEquals("OK", responseWrapper.getStatusText());
    }

    @Test
    void getResponseBodyAsBytes() throws IOException {
        assertArrayEquals("Test content".getBytes(), responseWrapper.getResponseBodyAsBytes());
    }

    @Test
    void getResponseBodyAsByteBuffer() throws IOException {
        assertEquals("Test content", new String(responseWrapper.getResponseBodyAsByteBuffer().array()));
    }

    @Test
    void getResponseBodyAsStream() throws IOException {
        byte[] bytes = new byte[responseWrapper.getResponseBodyAsStream().available()];
        responseWrapper.getResponseBodyAsStream().read(bytes);
        assertEquals("Test content", new String(bytes));
    }

    @Test
    void getResponseBodyExcerpt() throws IOException {
        assertEquals("Test", responseWrapper.getResponseBodyExcerpt(4));
    }

    @Test
    void getResponseBody() throws IOException {
        assertEquals("Test content", responseWrapper.getResponseBody());
    }

    @Test
    void getResponseBodyExcerptWithCharset() throws IOException {
        assertEquals("Test", responseWrapper.getResponseBodyExcerpt(4, "UTF-8"));
    }

    @Test
    void getUri() {
        assertNull(responseWrapper.getUri());
    }

    @Test
    void getContentType() {
        assertEquals("application/json", responseWrapper.getContentType());
    }

    @Test
    void getHeader() {
        assertEquals("application/json", responseWrapper.getHeader("Content-Type"));
    }

    @Test
    void getHeaders() {
        assertEquals(3, responseWrapper.getHeaders().size());
        assertEquals("application/json", responseWrapper.getHeaders("Content-Type").get(0));
    }

    @Test
    void isRedirected() {
        assertFalse(responseWrapper.isRedirected());
    }

    @Test
    void getCookies() {
        assertEquals(2, responseWrapper.getCookies().size());
    }

    @Test
    void hasResponseStatus() {
        assertTrue(responseWrapper.hasResponseStatus());
    }

    @Test
    void hasResponseHeaders() {
        assertTrue(responseWrapper.hasResponseHeaders());
    }

    @Test
    void hasResponseBody() {
        assertTrue(responseWrapper.hasResponseBody());
    }

}
