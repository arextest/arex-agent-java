package io.arex.inst.netty.v4.server;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.netty.v4.common.AttributeKey;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.Map;

public class ResponseTracingHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (!ContextManager.needRecordOrReplay()) {
            ctx.write(msg, promise);
            return;
        }

        try {
            ChannelPromise prm = promise;
            if (msg instanceof LastHttpContent) {
                if (msg instanceof FullHttpResponse) {
                    processHeaders(ctx.channel(), (HttpResponse) msg);
                }

                // compatible with VoidChannelPromise.java package not visible (< 4.1.0)
                if (prm.getClass().getName().contains("VoidChannelPromise")) {
                    prm = ctx.newPromise();
                }

                String body = NettyHelper.parseBody(((LastHttpContent) msg).content());
                // record response and save record on RequestTracingHandler#channelReadComplete
                invoke(ctx.channel(), body, prm.cause());
            } else {
                if (msg instanceof HttpResponse) {
                    processHeaders(ctx.channel(), (HttpResponse) msg);
                }
            }
        } catch (Throwable e) {
            LogManager.warn("netty write error", e);
        } finally {
            ctx.write(msg, promise);
        }
    }

    private void processHeaders(final Channel channel, final HttpResponse response) {
        Mocker mocker = channel.attr(AttributeKey.TRACING_MOCKER).get();
        if (mocker == null) {
            return;
        }
        Map<String, String> headers = NettyHelper.parseHeaders(response.headers());
        mocker.getTargetResponse().setAttribute("Headers", headers);
        channel.attr(AttributeKey.TRACING_MOCKER).set(mocker);
        appendHeader(response);
    }

    private void appendHeader(HttpResponse response) {
        if (ContextManager.needRecord()) {
            response.headers().set(ArexConstants.RECORD_ID, ContextManager.currentContext().getCaseId());
            return;
        }

        if (ContextManager.needReplay()) {
            response.headers().set(ArexConstants.REPLAY_ID, ContextManager.currentContext().getReplayId());
        }
    }

    private void invoke(final Channel channel, final String content, Throwable throwable) {
        Mocker mocker = channel.attr(AttributeKey.TRACING_MOCKER).get();
        Object response = throwable != null ? throwable : content;
        if (mocker == null || response == null) {
            return;
        }
        mocker.getTargetResponse().setBody(Serializer.serialize(response));
        mocker.getTargetResponse().setType(TypeUtil.getName(response));
    }
}