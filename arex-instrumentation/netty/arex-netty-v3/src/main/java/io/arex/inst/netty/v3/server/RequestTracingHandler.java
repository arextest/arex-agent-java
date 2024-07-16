package io.arex.inst.netty.v3.server;

import io.arex.agent.bootstrap.constants.ConfigConstants;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.netty.v3.common.NettyHelper;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventProcessor;
import io.arex.inst.runtime.listener.EventSource;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class RequestTracingHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        try {
            if (event.getMessage() instanceof HttpRequest) {
                // init
                CaseEventDispatcher.onEvent(CaseEvent.ofEnterEvent());
                HttpRequest request = (HttpRequest) event.getMessage();
                String caseId = NettyHelper.getHeader(request, ArexConstants.RECORD_ID);
                if (shouldSkip(request, caseId)) {
                    return;
                }

                String excludeMockTemplate = NettyHelper.getHeader(request, ArexConstants.HEADER_EXCLUDE_MOCK);
                CaseEventDispatcher.onEvent(CaseEvent.ofCreateEvent(EventSource.of(caseId, excludeMockTemplate)));
                ContextManager.currentContext().setAttachment(ArexConstants.FORCE_RECORD, NettyHelper.getHeader(request, ArexConstants.FORCE_RECORD));
                if (ContextManager.needRecordOrReplay()) {
                    Mocker mocker = MockUtils.createNettyProvider(request.getUri());
                    Mocker.Target target = mocker.getTargetRequest();
                    target.setAttribute("HttpMethod", request.getMethod().getName());
                    target.setAttribute("Headers", NettyHelper.parseHeaders(NettyHelper.getHeaders(request)));

                    // save request body, if the request body too large, it will be separated into multiple HttpContent
                    setContent(request.getContent(), mocker);
                    // cache mocker for writeComplete and writeRequested, not use ctx.setAttachment(mocker), because it maybe used by user handler
                    ContextManager.currentContext().setAttachment("arex-netty-server-mocker", mocker);
                }
            } else if (event.getMessage() instanceof HttpChunk) {
                Mocker mocker = (Mocker) ContextManager.currentContext().getAttachment("arex-netty-server-mocker");
                if (mocker != null) {
                    setContent(((HttpChunk) event.getMessage()).getContent(), mocker);
                }
            }
        } catch (Throwable e) {
            LogManager.warn("netty messageReceived error", e);
        } finally {
            super.messageReceived(ctx, event);
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

    private boolean shouldSkip(HttpRequest request, String caseId) {
        if (!EventProcessor.dependencyInitComplete()) {
            return true;
        }
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
