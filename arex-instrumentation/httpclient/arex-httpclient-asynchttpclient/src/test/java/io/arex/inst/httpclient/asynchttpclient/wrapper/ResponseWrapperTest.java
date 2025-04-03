package io.arex.inst.httpclient.asynchttpclient.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.netty.EagerResponseBodyPart;
import org.asynchttpclient.uri.Uri;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

class ResponseWrapperTest {

    private static ResponseWrapper responseWrapper;
    private static HttpResponseStatus responseStatus;
    private static HttpHeaders httpHeaders;
    private static HttpResponseBodyPart bodyPart;

    @BeforeAll
    static void setUp() {
        responseStatus = Mockito.mock(HttpResponseStatus.class);
        final Uri uri = Mockito.mock(Uri.class);
        Mockito.when(uri.toString()).thenReturn("http://localhost/");
        Mockito.when(responseStatus.getStatusCode()).thenReturn(200);
        Mockito.when(responseStatus.getStatusText()).thenReturn("OK");
        Mockito.when(responseStatus.getProtocolName()).thenReturn("HTTP");
        Mockito.when(responseStatus.getProtocolMajorVersion()).thenReturn(1);
        Mockito.when(responseStatus.getProtocolMinorVersion()).thenReturn(0);
        Mockito.when(responseStatus.getProtocolText()).thenReturn("HTTP/1.1");
        Mockito.when(responseStatus.getRemoteAddress()).thenReturn(new SocketAddressWrapper("remoteAddress"));
        Mockito.when(responseStatus.getLocalAddress()).thenReturn(new SocketAddressWrapper("localAddress"));
        Mockito.when(responseStatus.getUri()).thenReturn(uri);

        httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Content-Length", 100);
        httpHeaders.add("test", Arrays.asList("test1", "test2"));

        byte[] content = "test".getBytes();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(ByteBuffer.wrap(content));
        final EagerResponseBodyPart bodyPart = new EagerResponseBodyPart(byteBuf, true);
        responseWrapper = new ResponseWrapper();
        responseWrapper.setHttpStatus(responseStatus);
        responseWrapper.setHttpHeaders(httpHeaders);
        responseWrapper.setHttpResponseBody(Arrays.asList(bodyPart));
    }

    @Test
    void getContent() {
        assertEquals("test", new String(responseWrapper.getContent()));
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
    void getHeaders() {
        assertEquals(3, responseWrapper.getHeaders().size());
    }

    @Test
    void getLocalAddress() {
        assertEquals("localAddress", responseWrapper.getLocalAddress());
    }

    @Test
    void getRemoteAddress() {
        assertEquals("remoteAddress", responseWrapper.getRemoteAddress());
    }

    @Test
    void getUri() {
        assertEquals("http://localhost/", responseWrapper.getUri());
    }

    @Test
    void getProtocolMajorVersion() {
        assertEquals(1, responseWrapper.getProtocolMajorVersion());
    }

    @Test
    void getProtocolMinorVersion() {
        assertEquals(0, responseWrapper.getProtocolMinorVersion());
    }

    @Test
    void getProtocolName() {
        assertEquals("HTTP", responseWrapper.getProtocolName());
    }

    @Test
    void getProtocolText() {
        assertEquals("HTTP/1.1", responseWrapper.getProtocolText());
    }
}