package io.arex.inst.httpclient.asynchttpclient.wrapper;

import static org.junit.jupiter.api.Assertions.*;

import org.asynchttpclient.uri.Uri;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ResponseStatusWrapperTest {
    private static ResponseWrapper responseWrapper;
    private static ResponseStatusWrapper responseStatusWrapper;
    private static Uri uri;

    @BeforeAll
    static void setUp() {
        uri = Mockito.mock(Uri.class);
        responseWrapper = Mockito.mock(ResponseWrapper.class);
        Mockito.when(responseWrapper.getStatusCode()).thenReturn(200);
        Mockito.when(responseWrapper.getStatusText()).thenReturn("OK");
        Mockito.when(responseWrapper.getProtocolName()).thenReturn("HTTP");
        Mockito.when(responseWrapper.getProtocolMajorVersion()).thenReturn(1);
        Mockito.when(responseWrapper.getProtocolMinorVersion()).thenReturn(0);
        Mockito.when(responseWrapper.getProtocolText()).thenReturn("HTTP/1.1");
        Mockito.when(responseWrapper.getRemoteAddress()).thenReturn("remoteAddress");
        Mockito.when(responseWrapper.getLocalAddress()).thenReturn("localAddress");
        responseStatusWrapper = new ResponseStatusWrapper(uri, responseWrapper);
    }

    @AfterAll
    static void tearDown() {
        uri = null;
        responseWrapper = null;
        responseStatusWrapper = null;
        Mockito.clearAllCaches();
    }

    @Test
    void getStatusCode() {
        assertEquals(200, responseStatusWrapper.getStatusCode());
    }

    @Test
    void getStatusText() {
        assertEquals("OK", responseStatusWrapper.getStatusText());
    }

    @Test
    void getProtocolName() {
        assertEquals("HTTP", responseStatusWrapper.getProtocolName());
    }

    @Test
    void getProtocolMajorVersion() {
        assertEquals(1, responseStatusWrapper.getProtocolMajorVersion());
    }

    @Test
    void getProtocolMinorVersion() {
        assertEquals(0, responseStatusWrapper.getProtocolMinorVersion());
    }

    @Test
    void getProtocolText() {
        assertEquals("HTTP/1.1", responseStatusWrapper.getProtocolText());
    }

    @Test
    void getRemoteAddress() {
        assertEquals("remoteAddress", responseStatusWrapper.getRemoteAddress().toString());
    }

    @Test
    void getLocalAddress() {
        assertEquals("localAddress", responseStatusWrapper.getLocalAddress().toString());
    }
}