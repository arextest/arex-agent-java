package io.arex.inst.netty.v4.server;

import com.arextest.model.constants.MockAttributeNames;
import com.arextest.model.mock.AREXMocker;
import com.arextest.model.mock.Mocker;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.listener.CaseEvent;
import io.arex.foundation.listener.CaseInitializer;
import io.arex.foundation.listener.CaseListenerImpl;
import io.arex.foundation.listener.EventSource;
import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.Constants;
import io.arex.foundation.model.MockerUtils;
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
        CaseInitializer.onEnter();
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String caseId = request.headers().get(Constants.RECORD_ID);
            if (shouldSkip(request, caseId)) {
                ctx.fireChannelRead(msg);
                return;
            }

            String excludeMockTemplate = request.headers().get(Constants.HEADER_EXCLUDE_MOCK);
            CaseListenerImpl.INSTANCE.onEvent(
                    new CaseEvent(EventSource.of(caseId, excludeMockTemplate), CaseEvent.Action.CREATE));
            if (ContextManager.needRecordOrReplay()) {
                AREXMocker mocker = MockerUtils.createServlet(request.uri());
                Mocker.Target target=mocker.getTargetRequest();
                target.setAttribute(MockAttributeNames.HTTP_METHOD,request.method().name());
                target.setAttribute(MockAttributeNames.HEADERS,NettyHelper.parseHeaders(request.headers()));
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

        String forceRecord = request.headers().get(Constants.FORCE_RECORD);
        // Do not skip if header with arex-force-record=true
        if (StringUtil.isEmpty(caseId) && Boolean.parseBoolean(forceRecord)) {
            return false;
        }

        // Skip if request header with arex-replay-warm-up=true
        if (Boolean.parseBoolean(request.headers().get(Constants.REPLAY_WARM_UP))) {
            return true;
        }

        if (CaseInitializer.exceedRecordRate(request.uri())) {
            return true;
        }

        return IgnoreService.isServiceEnabled(request.method().toString() + " " + request.uri());
    }
}