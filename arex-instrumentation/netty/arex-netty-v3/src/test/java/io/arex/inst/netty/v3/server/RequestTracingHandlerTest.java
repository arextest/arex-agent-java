package io.arex.inst.netty.v3.server;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.Assert;
import io.arex.inst.netty.v3.common.NettyHelper;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class RequestTracingHandlerTest {
    static RequestTracingHandler target;
    static ChannelHandlerContext ctx;
    static HttpRequest request;

    @BeforeAll
    static void setUp() {
        target = new RequestTracingHandler();
        ctx = Mockito.mock(ChannelHandlerContext.class);
        request = Mockito.mock(HttpRequest.class);
        Mockito.when(request.getMethod()).thenReturn(Mockito.mock(HttpMethod.class));
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(IgnoreUtils.class);
        Mockito.mockStatic(NettyHelper.class);
        Mockito.mockStatic(RecordLimiter.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
        Mockito.mockStatic(MockUtils.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock"));
    }

    @AfterAll
    static void tearDown() {
        target = null;
        ctx = null;
        request = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("channelReadCase")
    void channelRead(Runnable mocker, MessageEvent event) throws Exception {
        mocker.run();
        target.messageReceived(ctx, event);
        verify(ctx, atLeastOnce()).sendUpstream(any());
    }

    static Stream<Arguments> channelReadCase() {
        MessageEvent event = Mockito.mock(MessageEvent.class);
        Runnable mocker1 = () -> {
            Mockito.when(event.getMessage()).thenReturn(request);
            Mockito.when(NettyHelper.getHeader(request, ArexConstants.RECORD_ID)).thenReturn("mock");
        };
        Runnable mocker2 = () -> {
            Mockito.when(NettyHelper.getHeader(request, ArexConstants.RECORD_ID)).thenReturn("");
            Mockito.when(NettyHelper.getHeader(request, ArexConstants.FORCE_RECORD)).thenReturn("true");
        };
        Runnable mocker3 = () -> {
            Mockito.when(NettyHelper.getHeader(request, ArexConstants.FORCE_RECORD)).thenReturn("false");
            Mockito.when(NettyHelper.getHeader(request, ArexConstants.REPLAY_WARM_UP)).thenReturn("true");
        };
        Runnable mocker4 = () -> {
            Mockito.when(NettyHelper.getHeader(request, ArexConstants.REPLAY_WARM_UP)).thenReturn("false");
        };
        Runnable mocker4_1 = () -> {
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(true);
        };
        Runnable mocker5 = () -> {
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(false);
            Mockito.when(RecordLimiter.acquire(any())).thenReturn(true);
        };
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetRequest(new Mocker.Target());
        mocker.setTargetResponse(new Mocker.Target());
        Runnable mocker6 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(MockUtils.createNettyProvider(any())).thenReturn(mocker);
            Mockito.when(NettyHelper.parseBody(any())).thenReturn("mock");
        };
        Runnable mocker7 = () -> {
            mocker.getTargetRequest().setBody("mock");
            Mockito.when(MockUtils.createNettyProvider(any())).thenReturn(mocker);
            Mockito.when(ContextManager.currentContext()).thenReturn(Mockito.mock(ArexContext.class));
        };

        return Stream.of(
                arguments(mocker1, event),
                arguments(mocker2, event),
                arguments(mocker3, event),
                arguments(mocker4, event),
                arguments(mocker4_1, event),
                arguments(mocker5, event),
                arguments(mocker6, event),
                arguments(mocker7, event)
        );
    }

    @ParameterizedTest
    @MethodSource("writeCompleteCase")
    void writeComplete(Runnable mocker, Assert asserts) throws Exception {
        mocker.run();
        target.writeComplete(ctx, null);
        asserts.verity();
    }

    static Stream<Arguments> writeCompleteCase() {
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.currentContext()).thenReturn(null);
        };
        ArexContext context = Mockito.mock(ArexContext.class);
        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
        };
        ArexMocker mocker = new ArexMocker();
        Runnable mocker3 = () -> {
            mocker.setTargetRequest(new Mocker.Target());
            mocker.setTargetResponse(new Mocker.Target());
            Mockito.when(context.getAttachment(any())).thenReturn(mocker);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Runnable mocker4 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(false);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        MockedStatic<CaseEventDispatcher> mockCaseEvent = Mockito.mockStatic(CaseEventDispatcher.class);
        Assert asserts1 = () -> {
            mockCaseEvent.verify(() -> CaseEventDispatcher.onEvent(any()), times(0));
        };
        Assert asserts2 = () -> {
            mockCaseEvent.verify(() -> CaseEventDispatcher.onEvent(any()), atLeastOnce());
        };

        return Stream.of(
                arguments(mocker1, asserts1),
                arguments(mocker2, asserts1),
                arguments(mocker3, asserts2),
                arguments(mocker4, asserts2)
        );
    }
}