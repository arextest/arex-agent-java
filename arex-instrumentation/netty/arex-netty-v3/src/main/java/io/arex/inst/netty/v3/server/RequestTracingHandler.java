package io.arex.inst.netty.v3.server;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.netty.v3.common.NettyHelper;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventSource;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;

public class RequestTracingHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) {
        try {
            if (event.getMessage() instanceof HttpRequest) {
                // init
                CaseEventDispatcher.onEvent(CaseEvent.ofEnterEvent());
                HttpRequest request = (HttpRequest) event.getMessage();
                String caseId = NettyHelper.getHeader(request, ArexConstants.RECORD_ID);
                if (shouldSkip(request, caseId)) {
                    ctx.sendUpstream(event);
                    return;
                }

                String excludeMockTemplate = NettyHelper.getHeader(request, ArexConstants.HEADER_EXCLUDE_MOCK);
                CaseEventDispatcher.onEvent(CaseEvent.ofCreateEvent(EventSource.of(caseId, excludeMockTemplate)));
                if (ContextManager.needRecordOrReplay()) {
                    Mocker mocker = MockUtils.createNettyProvider(request.getUri());
                    Mocker.Target target = mocker.getTargetRequest();
                    target.setAttribute("HttpMethod", request.getMethod().getName());
                    target.setAttribute("Headers", NettyHelper.parseHeaders(NettyHelper.getHeaders(request)));

                    // save request body, if the request body too large, it will be separated into multiple HttpContent
                    setContent(request.getContent(), mocker);
                    // cache mocker for writeComplete and writeRequested, not use ctx.setAttachment(mocker), because it maybe used by user handler
                    ContextManager.currentContext().setAttachment(ArexConstants.NETTY_TRACING_MOCKER, mocker);
                }
            }
        } catch (Throwable e) {
            LogManager.warn("netty messageReceived error", e);
        } finally {
            ctx.sendUpstream(event);
        }
    }

    private void setContent(ChannelBuffer contentBuffer, Mocker mocker) {
        String content = NettyHelper.parseBody(contentBuffer);
        if (content != null) {
            String requestBody = mocker.getTargetRequest().getBody();
            if (StringUtil.isNotEmpty(requestBody)) {
                requestBody += content;
            } else {
                requestBody = content;
            }
            mocker.getTargetRequest().setBody(requestBody);
        }
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent event) {
        try {
            ArexContext context = ContextManager.currentContext();
            if (context == null) {
                return;
            }
            Object mockerObj = context.getAttachment(ArexConstants.NETTY_TRACING_MOCKER);
            if (mockerObj == null) {
                return;
            }
            Mocker mocker = (Mocker) mockerObj;
            if (ContextManager.needReplay()) {
                MockUtils.replayBody(mocker);
            } else if (ContextManager.needRecord()) {
                MockUtils.recordMocker(mocker);
            }
            CaseEventDispatcher.onEvent(CaseEvent.ofExitEvent());
            // clear mocker
            context.setAttachment(ArexConstants.NETTY_TRACING_MOCKER, null);
        } catch (Throwable e) {
            LogManager.warn("netty writeComplete error", e);
        } finally {
            ctx.sendUpstream(event);
        }
    }

    private boolean shouldSkip(HttpRequest request, String caseId) {
        // Replay scene
        if (StringUtil.isNotEmpty(caseId)) {
            return Config.get().getBoolean(ConfigConstants.DISABLE_REPLAY, false);
        }

        String forceRecord = NettyHelper.getHeader(request, ArexConstants.FORCE_RECORD);
        // Do not skip if header with arex-force-record=true
        if (StringUtil.isEmpty(caseId) && Boolean.parseBoolean(forceRecord)) {
            return false;
        }

        // Skip if request header with arex-replay-warm-up=true
        if (Boolean.parseBoolean(NettyHelper.getHeader(request, ArexConstants.REPLAY_WARM_UP))) {
            return true;
        }

        if (IgnoreUtils.excludeEntranceOperation(request.getUri())) {
            return true;
        }

        return Config.get().invalidRecord(request.getUri());
    }
}