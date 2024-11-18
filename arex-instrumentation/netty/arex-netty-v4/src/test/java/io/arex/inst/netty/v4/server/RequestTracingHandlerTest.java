package io.arex.inst.netty.v4.server;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.agent.bootstrap.util.Assert;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.listener.EventProcessor;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.arex.inst.runtime.util.CaseManager;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
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
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RequestTracingHandlerTest {
    static RequestTracingHandler target;
    static ChannelHandlerContext ctx;
    static HttpRequest request;
    static HttpHeaders headers;
    static MockedStatic<CaseEventDispatcher> mockCaseEvent;

    @BeforeAll
    static void setUp() {
        target = new RequestTracingHandler();
        ctx = Mockito.mock(ChannelHandlerContext.class);
        request = Mockito.mock(HttpRequest.class);
        headers = Mockito.mock(HttpHeaders.class);
        Mockito.when(request.headers()).thenReturn(headers);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(IgnoreUtils.class);
        Mockito.mockStatic(NettyHelper.class);
        Mockito.mockStatic(RecordLimiter.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
        mockCaseEvent = Mockito.mockStatic(CaseEventDispatcher.class);
        Mockito.mockStatic(MockUtils.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock"));
        Mockito.mockStatic(EventProcessor.class);
        Mockito.when(EventProcessor.dependencyInitComplete()).thenReturn(true);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        ctx = null;
        request = null;
        headers = null;
        mockCaseEvent = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("channelReadCase")
    void channelRead(Runnable mocker, Object msg) throws Exception {
        mocker.run();
        target.channelRead(ctx, msg);
        verify(ctx, atLeastOnce()).fireChannelRead(any());
    }

    static Stream<Arguments> channelReadCase() {
        Runnable mocker1 = () -> {
            Mockito.when(headers.get(ArexConstants.RECORD_ID)).thenReturn("mock");
        };
        Runnable mocker2 = () -> {
            Mockito.when(headers.get(ArexConstants.RECORD_ID)).thenReturn("");
            Mockito.when(headers.get(ArexConstants.FORCE_RECORD)).thenReturn("true");
            Mockito.when(request.getMethod()).thenReturn(HttpMethod.POST);
        };
        Runnable mocker3 = () -> {
            Mockito.when(headers.get(ArexConstants.FORCE_RECORD)).thenReturn("false");
            Mockito.when(headers.get(ArexConstants.REPLAY_WARM_UP)).thenReturn("true");
        };
        Runnable mocker4 = () -> {
            Mockito.when(headers.get(ArexConstants.REPLAY_WARM_UP)).thenReturn("false");
        };
        Runnable mocker4_1 = () -> {
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(true);
        };
        Runnable mocker5 = () -> {
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(false);
            Mockito.when(RecordLimiter.acquire(any())).thenReturn(true);
        };
        Channel channel = Mockito.mock(Channel.class);
        Attribute attribute = Mockito.mock(Attribute.class);
        ArexMocker mocker = new ArexMocker();
        Runnable mocker6 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ctx.channel()).thenReturn(channel);
            Mockito.when(channel.attr(any())).thenReturn(attribute);
            Mockito.when(MockUtils.createNettyProvider(any())).thenReturn(mocker);
        };

        LastHttpContent httpContent = Mockito.mock(LastHttpContent.class);
        Runnable mocker7 = () -> {
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            Mockito.when(attribute.get()).thenReturn(mocker);
            Mockito.when(httpContent.content()).thenReturn(new EmptyByteBuf(new UnpooledByteBufAllocator(false)));
        };

        Runnable mocker8 = () -> {
            Mockito.when(httpContent.content()).thenReturn(UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes("mock".getBytes()));
        };
        Runnable mocker9 = () -> {
            mocker.getTargetRequest().setBody("mock");
        };
        Runnable mocker10 = () -> {
            Mockito.when(EventProcessor.dependencyInitComplete()).thenReturn(false);
        };

        return Stream.of(
                arguments(mocker1, request),
                arguments(mocker2, request),
                arguments(mocker3, request),
                arguments(mocker4, request),
                arguments(mocker4_1, request),
                arguments(mocker5, request),
                arguments(mocker6, request),
                arguments(mocker7, httpContent),
                arguments(mocker8, httpContent),
                arguments(mocker9, httpContent),
                arguments(mocker10, request)
        );
    }
}
