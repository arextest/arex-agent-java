package io.arex.inst.netty.v3.server;

import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class CombinedSimpleChannelHandlerTest {

    static CombinedSimpleChannelHandler<?, ?> target = null;
    @BeforeAll
    static void setUp() {
        SimpleChannelUpstreamHandler upstream = Mockito.mock(SimpleChannelUpstreamHandler.class);
        SimpleChannelDownstreamHandler downstream = Mockito.mock(SimpleChannelDownstreamHandler.class);
        target = new CombinedSimpleChannelHandler<>(upstream, downstream);
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void handleUpstream() {
        assertDoesNotThrow(() -> target.handleUpstream(null, null));
    }

    @Test
    void messageReceived() {
        assertDoesNotThrow(() -> target.messageReceived(null, null));
    }

    @Test
    void exceptionCaught() {
        assertDoesNotThrow(() -> target.exceptionCaught(null, null));
    }

    @Test
    void channelOpen() {
        assertDoesNotThrow(() -> target.channelOpen(null, null));
    }

    @Test
    void channelBound() {
        assertDoesNotThrow(() -> target.channelBound(null, null));
    }

    @Test
    void channelConnected() {
        assertDoesNotThrow(() -> target.channelConnected(null, null));
    }

    @Test
    void channelInterestChanged() {
        assertDoesNotThrow(() -> target.channelInterestChanged(null, null));
    }

    @Test
    void channelDisconnected() {
        assertDoesNotThrow(() -> target.channelDisconnected(null, null));
    }

    @Test
    void channelUnbound() {
        assertDoesNotThrow(() -> target.channelUnbound(null, null));
    }

    @Test
    void channelClosed() {
        assertDoesNotThrow(() -> target.channelClosed(null, null));
    }

    @Test
    void writeComplete() {
        assertDoesNotThrow(() -> target.writeComplete(null, null));
    }

    @Test
    void childChannelOpen() {
        assertDoesNotThrow(() -> target.childChannelOpen(null, null));
    }

    @Test
    void childChannelClosed() {
        assertDoesNotThrow(() -> target.childChannelClosed(null, null));
    }

    @Test
    void handleDownstream() {
        assertDoesNotThrow(() -> target.handleDownstream(null, null));
    }

    @Test
    void writeRequested() {
        assertDoesNotThrow(() -> target.writeRequested(null, null));
    }

    @Test
    void bindRequested() {
        assertDoesNotThrow(() -> target.bindRequested(null, null));
    }

    @Test
    void connectRequested() {
        assertDoesNotThrow(() -> target.connectRequested(null, null));
    }

    @Test
    void setInterestOpsRequested() {
        assertDoesNotThrow(() -> target.setInterestOpsRequested(null, null));
    }

    @Test
    void disconnectRequested() {
        assertDoesNotThrow(() -> target.disconnectRequested(null, null));
    }

    @Test
    void unbindRequested() {
        assertDoesNotThrow(() -> target.unbindRequested(null, null));
    }

    @Test
    void closeRequested() {
        assertDoesNotThrow(() -> target.closeRequested(null, null));
    }
}