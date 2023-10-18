package io.arex.inst.netty.v3.server;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.netty.v3.common.NettyHelper;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.TypeUtil;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;

import java.util.Map;

public class ResponseTracingHandler extends SimpleChannelDownstreamHandler {

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent event) {
        if (!ContextManager.needRecordOrReplay()) {
            ctx.sendDownstream(event);
            return;
        }

        try {
            if (event.getMessage() instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) event.getMessage();
                Object mockerObj = ContextManager.currentContext().getAttachment("arex-netty-server-mocker");
                if (mockerObj == null) {
                    return;
                }
                Mocker mocker = (Mocker) mockerObj;
                processHeaders(mocker, response);
                String body = NettyHelper.parseBody(response.getContent());
                // record response and save record on RequestTracingHandler#channelReadComplete
                Throwable throwable = event.getFuture() != null && event.getFuture().getCause() != null ? event.getFuture().getCause() : null;
                invoke(mocker, body, throwable);
            }
        } catch (Throwable e) {
            LogManager.warn("netty writeRequested error", e);
        } finally {
            ctx.sendDownstream(event);
        }
    }

    private void processHeaders(Mocker mocker, HttpResponse response) {
        if (mocker == null) {
            return;
        }
        Map<String, String> headers = NettyHelper.parseHeaders(NettyHelper.getHeaders(response));
        mocker.getTargetResponse().setAttribute("Headers", headers);
        appendHeader(response);
    }

    private void appendHeader(HttpResponse response) {
        if (ContextManager.needRecord()) {
            NettyHelper.setHeader(response, ArexConstants.RECORD_ID, ContextManager.currentContext().getCaseId());
            return;
        }

        if (ContextManager.needReplay()) {
            NettyHelper.setHeader(response, ArexConstants.REPLAY_ID, ContextManager.currentContext().getReplayId());
        }
    }

    private void invoke(Mocker mocker, String content, Throwable throwable) {
        Object response = throwable != null ? throwable : content;
        if (mocker == null || response == null) {
            return;
        }
        mocker.getTargetResponse().setBody(Serializer.serialize(response));
        mocker.getTargetResponse().setType(TypeUtil.getName(response));
    }
}