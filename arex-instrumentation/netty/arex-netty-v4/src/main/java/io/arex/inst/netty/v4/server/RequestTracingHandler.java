package io.arex.inst.netty.v4.server;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventSource;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.netty.v4.common.AttributeKey;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.CharsetUtil;

public class RequestTracingHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // init
            if (msg instanceof HttpRequest) {
                CaseEventDispatcher.onEvent(CaseEvent.ofEnterEvent());
                HttpRequest request = (HttpRequest) msg;
                String caseId = request.headers().get(ArexConstants.RECORD_ID);
                if (shouldSkip(request, caseId)) {
                    ctx.fireChannelRead(msg);
                    return;
                }

                String excludeMockTemplate = request.headers().get(ArexConstants.HEADER_EXCLUDE_MOCK);
                CaseEventDispatcher.onEvent(CaseEvent.ofCreateEvent(EventSource.of(caseId, excludeMockTemplate)));
                if (ContextManager.needRecordOrReplay()) {
                    Mocker mocker = MockUtils.createNettyProvider(request.getUri());
                    Mocker.Target target = mocker.getTargetRequest();
                    target.setAttribute("HttpMethod", request.getMethod().name());
                    target.setAttribute("Headers", NettyHelper.parseHeaders(request.headers()));
                    ctx.channel().attr(AttributeKey.TRACING_MOCKER).set(mocker);
                }
            }

            // record request body, if the request body too large, it will be separated into multiple HttpContent
            if (msg instanceof HttpContent) {
                recordBody(ctx, (HttpContent) msg);
            }
        } catch (Throwable e) {
            LogManager.warn("netty read error", e);
        } finally {
            ctx.fireChannelRead(msg);
        }
    }

    private void recordBody(ChannelHandlerContext ctx, HttpContent httpContent) {
        Mocker mocker = ctx.channel().attr(AttributeKey.TRACING_MOCKER).get();
        if (mocker == null) {
            return;
        }
        String content = httpContent.content().toString(CharsetUtil.UTF_8);
        if (StringUtil.isEmpty(content)) {
            return;
        }
        String requestBody = mocker.getTargetRequest().getBody();
        if (StringUtil.isNotEmpty(requestBody)) {
            requestBody += content;
        } else {
            requestBody = content;
        }
        mocker.getTargetRequest().setBody(requestBody);
    }

    private boolean shouldSkip(HttpRequest request, String caseId) {
        // Replay scene
        if (StringUtil.isNotEmpty(caseId)) {
            return Config.get().getBoolean(ConfigConstants.DISABLE_REPLAY, false);
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

        if (IgnoreUtils.excludeEntranceOperation(request.getUri())) {
            return true;
        }

        return Config.get().invalidRecord(request.getUri());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        try {
            Mocker mocker = ctx.channel().attr(AttributeKey.TRACING_MOCKER).getAndSet(null);
            if (mocker == null) {
                return;
            }
            if (ContextManager.needReplay()) {
                MockUtils.replayBody(mocker);
            } else if (ContextManager.needRecord()) {
                MockUtils.recordMocker(mocker);
            }

            CaseEventDispatcher.onEvent(CaseEvent.ofExitEvent());
        } catch (Throwable e) {
            LogManager.warn("netty channelReadComplete error", e);
        } finally {
            super.channelReadComplete(ctx);
        }
    }
}