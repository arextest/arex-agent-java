package io.arex.inst.netty.v4.server;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.EventProcessor;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.model.AbstractMocker;
import io.arex.inst.runtime.model.Constants;
import io.arex.inst.runtime.model.ServiceEntranceMocker;
import io.arex.inst.runtime.serializer.Serializer;
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
        EventProcessor.onRequest();
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String caseId = request.headers().get(ArexConstants.RECORD_ID);
            if (shouldSkip(request, caseId)) {
                ctx.fireChannelRead(msg);
                return;
            }

            String excludeMockTemplate = request.headers().get(ArexConstants.HEADER_EXCLUDE_MOCK);
            CaseEventDispatcher.onEvent(
                new CaseEvent(StringUtil.defaultString(caseId), CaseEvent.Action.CREATE));
            if (ContextManager.needRecordOrReplay()) {
                ArexMocker mocker = MockService.createServlet(request.uri());
                Mocker.Target target = mocker.getTargetRequest();
                    target.setAttribute("HttpMethod", request.method().name());
                target.setAttribute("Headers", NettyHelper.parseHeaders(request.headers()));
                ctx.channel().attr(AttributeKey.TRACING_MOCKER).set(mocker);
            }
        }

        if (msg instanceof HttpContent) {
            LastHttpContent httpContent = (LastHttpContent) msg;
            Mocker mocker = ctx.channel().attr(AttributeKey.TRACING_MOCKER).get();
            if (mocker != null) {
                String content = NettyHelper.parseBody(httpContent.content());
                if (content != null) {
                    mocker.getTargetRequest().setBody(content);
                    ctx.channel().attr(AttributeKey.TRACING_MOCKER).set(mocker);
                }
            }
        }

        ctx.fireChannelRead(msg);
    }

    private boolean shouldSkip(HttpRequest request, String caseId) {
        // Replay scene
        if (StringUtil.isNotEmpty(caseId)) {
            return false;
        }

        String forceRecord = request.headers().get(ArexConstants.FORCE_RECORD);
        // Do not skip if header with arex-force-record=true
        if (StringUtil.isEmpty(caseId) && Boolean.parseBoolean(forceRecord)) {
            return false;
        }

        // Skip if request header with arex-replay-warm-up=true
        if (Boolean.parseBoolean(request.headers().get(ArexConstants.REPLAY_WARM_UP))) {
            return true;
        }

        if (RecordLimiter.acquire(request.uri())) {
            return true;
        }

        return IgnoreService.isServiceEnabled(request.method().toString() + " " + request.uri());
    }
}