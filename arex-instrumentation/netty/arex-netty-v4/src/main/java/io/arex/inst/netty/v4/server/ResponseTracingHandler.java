package io.arex.inst.netty.v4.server;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.netty.v4.common.AttributeKey;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.arex.inst.runtime.util.MockUtils;
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
        Mocker mocker = channel.attr(AttributeKey.TRACING_MOCKER).get();
        if (mocker == null) {
            return;
        }
        Map<String, String> headers=NettyHelper.parseHeaders(response.headers());
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

    private void invoke(final Channel channel, final String content) {
        if (StringUtil.isEmpty(content)) {
            return;
        }

        Mocker mocker = channel.attr(AttributeKey.TRACING_MOCKER).getAndRemove();
        if (mocker == null) {
            return;
        }
        mocker.getTargetResponse().setBody(content);
        if (ContextManager.needReplay()) {
           MockUtils.replayBody(mocker);
        } else if (ContextManager.needRecord()) {
            MockUtils.recordMocker(mocker);
        }

        CaseEventDispatcher.onEvent(CaseEvent.ofExitEvent());
    }
}