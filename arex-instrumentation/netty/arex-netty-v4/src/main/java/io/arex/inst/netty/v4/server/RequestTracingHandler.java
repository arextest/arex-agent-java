package io.arex.inst.netty.v4.server;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.healthy.HealthManager;
import io.arex.foundation.listener.CaseEvent;
import io.arex.foundation.listener.CaseListenerImpl;
import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.Constants;
import io.arex.foundation.model.ServiceEntranceMocker;
import io.arex.foundation.services.IgnoreService;
import io.arex.foundation.util.StringUtil;
import io.arex.inst.netty.v4.common.AttributeKey;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

public class RequestTracingHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String caseId = request.headers().get(Constants.RECORD_ID);
            if (shouldSkip(request, caseId)) {
                ctx.fireChannelRead(msg);
                return;
            }

            CaseListenerImpl.INSTANCE.onEvent(
                    new CaseEvent(StringUtil.isEmpty(caseId) ? StringUtil.EMPTY : caseId, CaseEvent.Action.CREATE));
            if (ContextManager.needRecordOrReplay()) {
                ServiceEntranceMocker mocker = new ServiceEntranceMocker();
                mocker.setMethod(request.method().name());
                mocker.setPath(request.uri());
                mocker.setRequestHeaders(NettyHelper.parseHeaders(request.headers()));

                ctx.channel().attr(AttributeKey.TRACING_MOCKER).set(mocker);
            }
        }

        if (msg instanceof HttpContent) {
            LastHttpContent httpContent = (LastHttpContent) msg;
            AbstractMocker mocker = ctx.channel().attr(AttributeKey.TRACING_MOCKER).get();
            if (mocker != null) {
                String content = NettyHelper.parseBody(httpContent.content());
                if (content != null) {
                    ((ServiceEntranceMocker) mocker).setRequest(content);
                    ctx.channel().attr(AttributeKey.TRACING_MOCKER).set(mocker);
                }
            }
        }

        ctx.fireChannelRead(msg);
    }

    private boolean shouldSkip(HttpRequest request, String caseId) {
        if ((StringUtil.isEmpty(caseId) && !checkRateLimit(request.uri()))) {
            return false;
        }

        if (Boolean.parseBoolean(request.headers().get(Constants.REPLAY_WARM_UP))) {
            return false;
        }
        return IgnoreService.isServiceEnabled(request.method().toString() + " " + request.uri());
    }

    private static boolean checkRateLimit(String path) {
        return ConfigManager.INSTANCE.isEnableDebug() ||
                HealthManager.acquire(path, ConfigManager.INSTANCE.getRecordRate());
    }
}
