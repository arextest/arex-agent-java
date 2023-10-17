package io.arex.inst.netty.v3.server;

import org.jboss.netty.channel.*;

public class CombinedSimpleChannelHandler<U extends SimpleChannelUpstreamHandler,
        D extends SimpleChannelDownstreamHandler> extends SimpleChannelHandler {
    private final U upstream;
    private final D downstream;

    public CombinedSimpleChannelHandler(U upstream, D downstream) {
        this.upstream = upstream;
        this.downstream = downstream;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        upstream.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        upstream.messageReceived(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        upstream.exceptionCaught(ctx, e);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        upstream.channelOpen(ctx, e);
    }

    @Override
    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        upstream.channelBound(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        upstream.channelConnected(ctx, e);
    }

    @Override
    public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        upstream.channelInterestChanged(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        upstream.channelDisconnected(ctx, e);
    }

    @Override
    public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        upstream.channelUnbound(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        upstream.channelClosed(ctx, e);
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
        upstream.writeComplete(ctx, e);
    }

    @Override
    public void childChannelOpen(ChannelHandlerContext ctx, ChildChannelStateEvent e)
            throws Exception {
        upstream.childChannelOpen(ctx, e);
    }

    @Override
    public void childChannelClosed(ChannelHandlerContext ctx, ChildChannelStateEvent e)
            throws Exception {
        upstream.childChannelClosed(ctx, e);
    }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        downstream.handleDownstream(ctx, e);
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        downstream.writeRequested(ctx, e);
    }

    @Override
    public void bindRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        downstream.bindRequested(ctx, e);
    }

    @Override
    public void connectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        downstream.connectRequested(ctx, e);
    }

    @Override
    public void setInterestOpsRequested(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        downstream.setInterestOpsRequested(ctx, e);
    }

    @Override
    public void disconnectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        downstream.disconnectRequested(ctx, e);
    }

    @Override
    public void unbindRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        downstream.unbindRequested(ctx, e);
    }

    @Override
    public void closeRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        downstream.closeRequested(ctx, e);
    }
}
