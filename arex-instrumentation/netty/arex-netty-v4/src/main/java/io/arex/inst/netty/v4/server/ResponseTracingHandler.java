package io.arex.inst.netty.v4.server;

import com.arextest.model.constants.MockAttributeNames;
import com.arextest.model.mock.Mocker;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.listener.CaseEvent;
import io.arex.foundation.listener.CaseListenerImpl;
import io.arex.foundation.model.Constants;
import io.arex.foundation.model.MockerUtils;
import io.arex.foundation.util.StringUtil;
import io.arex.inst.netty.v4.common.AttributeKey;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
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
        mocker.getTargetResponse().setAttribute(MockAttributeNames.HEADERS,headers);
        channel.attr(AttributeKey.TRACING_MOCKER).set(mocker);
        appendHeader(response);
    }

    private void appendHeader(HttpResponse response) {
        if (ContextManager.needRecord()) {
            response.headers().set(Constants.RECORD_ID, ContextManager.currentContext().getCaseId());
            return;
        }

        if (ContextManager.needReplay()) {
            response.headers().set(Constants.REPLAY_ID, ContextManager.currentContext().getReplayId());
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
           MockerUtils.replayBody(mocker);
        } else if (ContextManager.needRecord()) {
            MockerUtils.record(mocker);
        }

        CaseListenerImpl.INSTANCE.onEvent(new CaseEvent(this, CaseEvent.Action.DESTROY));
    }
}