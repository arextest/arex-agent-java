package io.arex.inst.netty.v3.server;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.netty.v3.common.NettyHelper;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.listener.CaseEvent;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpResponse;

import java.util.List;
import java.util.Map;

import static org.jboss.netty.buffer.ChannelBuffers.copiedBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

public class ResponseTracingHandler extends SimpleChannelDownstreamHandler {

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
        if (!ContextManager.needRecordOrReplay()) {
            super.writeRequested(ctx, event);
            return;
        }

        Object msg = event.getMessage();
        if (!(msg instanceof HttpMessage)) {
            super.writeRequested(ctx, event);
            return;
        }

        try {
            ctx.getChannel().write()
            Object mockerObj = ContextManager.currentContext().getAttachment("arex-netty-server-mocker");
            if (mockerObj == null) {
                return;
            }
            Mocker mocker = (Mocker) mockerObj;
            if (msg instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) msg;
                processHeaders(mocker, NettyHelper.getHeaders(response));
                appendHeader(response);
                String body = NettyHelper.parseBody(response.getContent());
                // record response and save record on RequestTracingHandler#channelReadComplete
                Throwable throwable = event.getFuture() != null && event.getFuture().getCause() != null ? event.getFuture().getCause() : null;
                invoke(mocker, body, throwable);
            } else if (msg instanceof HttpChunk) {
                HttpChunk chunk = (HttpChunk) msg;
                ChannelBuffer content = chunk.getContent();
                setResponseBodyContent(content, mocker);
                if (chunk.isLast()) {
                    if (chunk instanceof HttpChunkTrailer) {
                        HttpChunkTrailer httpChunkTrailer = (HttpChunkTrailer) chunk;
                        processHeaders(mocker, httpChunkTrailer.getHeaders());
                        httpChunkTrailer.getContent();
                    }
                }
            }
        } catch (Throwable e) {
            LogManager.warn("netty writeRequested error", e);
        } finally {
            super.writeRequested(ctx, event);
        }
    }

    private void processHeaders(Mocker mocker,  List<Map.Entry<String, String>> headerList) {
        if (mocker == null) {
            return;
        }
        Map<String, String> headers = NettyHelper.parseHeaders(headerList);
        Mocker.Target targetResponse = mocker.getTargetResponse();
        Object originHeaders = targetResponse.getAttribute("Headers");
        if (originHeaders instanceof Map) {
            //noinspection unchecked
            headers.putAll((Map<String, String>) originHeaders);
        }
        targetResponse.setAttribute("Headers", headers);
    }

    private void appendHeader(HttpMessage response) {
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

    private void setResponseBodyContent(ChannelBuffer contentBuffer, Mocker mocker) {
        String content = NettyHelper.parseBody(contentBuffer);
        if (content != null) {
            String resposneBody = mocker.getTargetResponse().getBody();
            if (StringUtil.isNotEmpty(resposneBody)) {
                resposneBody += content;
            } else {
                resposneBody = content;
            }
            mocker.getTargetResponse().setBody(resposneBody);
        }
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent event) throws Exception {
        try {
            ArexContext context = ContextManager.currentContext();
            if (context == null) {
                return;
            }
            Object mockerObj = context.getAttachment("arex-netty-server-mocker");
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
            context.setAttachment("arex-netty-server-mocker", null);
        } catch (Throwable e) {
            LogManager.warn("netty writeComplete error", e);
        } finally {
            super.writeComplete(ctx, event);
        }
    }
}