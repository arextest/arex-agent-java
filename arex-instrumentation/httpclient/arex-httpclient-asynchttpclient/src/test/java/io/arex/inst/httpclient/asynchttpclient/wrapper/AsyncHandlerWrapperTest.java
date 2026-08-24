package io.arex.inst.httpclient.asynchttpclient.wrapper;

import static org.mockito.ArgumentMatchers.any;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.netty.EagerResponseBodyPart;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AsyncHandlerWrapperTest {
    private static AsyncHandler handler;
    private static ResponseWrapper responseWrapper;
    private static AsyncHandlerWrapper asyncHandlerWrapper;

    @BeforeAll
    static void setUp() {
        handler = Mockito.mock(AsyncHandler.class);
        responseWrapper = Mockito.mock(ResponseWrapper.class);
        asyncHandlerWrapper = new AsyncHandlerWrapper(handler, responseWrapper);
    }

    @AfterAll
    static void tearDown() {
        handler = null;
        responseWrapper = null;
        asyncHandlerWrapper = null;
        Mockito.clearAllCaches();
    }


    @Test
    void onStatusReceived() throws Exception {
        asyncHandlerWrapper.onStatusReceived(null);
        Mockito.verify(handler, Mockito.times(1)).onStatusReceived(null);
    }

    @Test
    void onHeadersReceived() throws Exception {
        asyncHandlerWrapper.onHeadersReceived(null);
        Mockito.verify(handler, Mockito.times(1)).onHeadersReceived(null);
    }

    @Test
    void onBodyPartReceived() throws Exception {
        // null bodyPart
        asyncHandlerWrapper.onBodyPartReceived(null);
        Mockito.verify(handler, Mockito.times(1)).onBodyPartReceived(null);
        // bodyPart
        byte[] content = "test".getBytes();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(ByteBuffer.wrap(content));
        final EagerResponseBodyPart bodyPart = new EagerResponseBodyPart(byteBuf, true);
        asyncHandlerWrapper.onBodyPartReceived(bodyPart);
        Mockito.verify(handler, Mockito.times(1)).onBodyPartReceived(bodyPart);
    }

    @Test
    void onThrowable() {
        asyncHandlerWrapper.onThrowable(null);
        Mockito.verify(handler, Mockito.times(1)).onThrowable(null);
    }

    @Test
    void onCompleted() throws Exception {
        asyncHandlerWrapper.onCompleted();
        Mockito.verify(handler, Mockito.times(1)).onCompleted();
    }

    @Test
    void onTrailingHeadersReceived() throws Exception {
        asyncHandlerWrapper.onTrailingHeadersReceived(null);
        Mockito.verify(handler, Mockito.times(1)).onTrailingHeadersReceived(null);
    }

    @Test
    void onHostnameResolutionAttempt() {
        asyncHandlerWrapper.onHostnameResolutionAttempt(null);
        Mockito.verify(handler, Mockito.times(1)).onHostnameResolutionAttempt(null);
    }

    @Test
    void onHostnameResolutionSuccess() {
        asyncHandlerWrapper.onHostnameResolutionSuccess(null, null);
        Mockito.verify(handler, Mockito.times(1)).onHostnameResolutionSuccess(null, null);
    }

    @Test
    void onHostnameResolutionFailure() {
        asyncHandlerWrapper.onHostnameResolutionFailure(null, null);
        Mockito.verify(handler, Mockito.times(1)).onHostnameResolutionFailure(null, null);
    }

    @Test
    void onTcpConnectAttempt() {
        asyncHandlerWrapper.onTcpConnectAttempt(null);
        Mockito.verify(handler, Mockito.times(1)).onTcpConnectAttempt(null);
    }

    @Test
    void onTcpConnectSuccess() {
        asyncHandlerWrapper.onTcpConnectSuccess(null, null);
        Mockito.verify(handler, Mockito.times(1)).onTcpConnectSuccess(null, null);
    }

    @Test
    void onTcpConnectFailure() {
        asyncHandlerWrapper.onTcpConnectFailure(null, null);
        Mockito.verify(handler, Mockito.times(1)).onTcpConnectFailure(null, null);
    }

    @Test
    void onTlsHandshakeAttempt() {
        asyncHandlerWrapper.onTlsHandshakeAttempt();
        Mockito.verify(handler, Mockito.times(1)).onTlsHandshakeAttempt();
    }

    @Test
    void onTlsHandshakeSuccess() {
        asyncHandlerWrapper.onTlsHandshakeSuccess(null);
        Mockito.verify(handler, Mockito.times(1)).onTlsHandshakeSuccess(null);
    }

    @Test
    void onTlsHandshakeFailure() {
        asyncHandlerWrapper.onTlsHandshakeFailure(null);
        Mockito.verify(handler, Mockito.times(1)).onTlsHandshakeFailure(null);
    }

    @Test
    void onConnectionPoolAttempt() {
        asyncHandlerWrapper.onConnectionPoolAttempt();
        Mockito.verify(handler, Mockito.times(1)).onConnectionPoolAttempt();
    }

    @Test
    void onConnectionPooled() {
        asyncHandlerWrapper.onConnectionPooled(null);
        Mockito.verify(handler, Mockito.times(1)).onConnectionPooled(null);
    }

    @Test
    void onConnectionOffer() {
        asyncHandlerWrapper.onConnectionOffer(null);
        Mockito.verify(handler, Mockito.times(1)).onConnectionOffer(null);
    }

    @Test
    void onRequestSend() {
        asyncHandlerWrapper.onRequestSend(null);
        Mockito.verify(handler, Mockito.times(1)).onRequestSend(null);
    }

    @Test
    void onRetry() {
        asyncHandlerWrapper.onRetry();
        Mockito.verify(handler, Mockito.times(1)).onRetry();
    }
}