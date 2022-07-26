package io.arex.inst.netty.v4.server;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.listener.CaseEvent;
import io.arex.foundation.listener.CaseListenerImpl;
import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.Constants;
import io.arex.foundation.model.ServiceEntranceMocker;
import io.arex.foundation.util.StringUtil;
import io.arex.inst.netty.v4.common.AttributeKey;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

public class ResponseTracingHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (!ContextManager.needRecordOrReplay()) {
            ctx.write(msg, promise);
            return;
        }

        ChannelPromise prm = promise;
        try {
            if (msg instanceof LastHttpContent) {
                if (msg instanceof FullHttpResponse) {
                    processHeaders(ctx.channel(), (HttpResponse) msg);
                }

                if (prm.isVoid()) {
                    prm = ctx.newPromise();
                }

                String body = NettyHelper.parseBody(((LastHttpContent) msg).content());
                prm.addListener(future -> invoke(ctx.channel(), body));
            } else {
                if (msg instanceof HttpResponse) {
                    processHeaders(ctx.channel(), (HttpResponse) msg);
                }
            }
        } finally {
            ctx.write(msg, prm);
        }
    }

    private void processHeaders(final Channel channel, final HttpResponse response) {
        AbstractMocker mocker = channel.attr(AttributeKey.TRACING_MOCKER).get();
        if (mocker == null) {
            return;
        }

        ((ServiceEntranceMocker)mocker).setResponseHeaders(NettyHelper.parseHeaders(response.headers()));
        channel.attr(AttributeKey.TRACING_MOCKER).set(mocker);
        appendHeader(response);
    }

    private void appendHeader(HttpResponse response) {
        if (ContextManager.needRecord()) {
            response.headers().set(Constants.RECORD_ID, ContextManager.currentContext().getCaseId());
        }

        if (ContextManager.needReplay()) {
            response.headers().set(Constants.REPLAY_ID, ContextManager.currentContext().getReplayId());
        }
    }

    private void invoke(final Channel channel, final String content) {
        if (StringUtil.isEmpty(content)) {
            return;
        }

        AbstractMocker mocker = channel.attr(AttributeKey.TRACING_MOCKER).getAndRemove();
        if (mocker == null) {
            return;
        }
        mocker.setResponse(content);
        if (ContextManager.needReplay()) {
            mocker.replay();
        } else if (ContextManager.needRecord()) {
            mocker.record();
        }

        CaseListenerImpl.INSTANCE.onEvent(new CaseEvent(this, CaseEvent.Action.DESTROY));
    }
}
