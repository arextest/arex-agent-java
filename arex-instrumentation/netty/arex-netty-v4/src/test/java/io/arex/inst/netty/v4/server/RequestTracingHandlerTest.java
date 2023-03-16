package io.arex.inst.netty.v4.server;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.arex.inst.runtime.util.IgnoreUtils;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestTracingHandlerTest {
    static RequestTracingHandler target;
    static ChannelHandlerContext ctx;
    static HttpRequest request;
    static HttpHeaders headers;

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
    }

    @AfterAll
    static void tearDown() {
        target = null;
        ctx = null;
        request = null;
        headers = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("channelReadCase")
    void channelRead(Runnable mocker, Object msg) {
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
            Mockito.when(request.method()).thenReturn(HttpMethod.POST);
        };
        Runnable mocker3 = () -> {
            Mockito.when(headers.get(ArexConstants.FORCE_RECORD)).thenReturn("false");
            Mockito.when(headers.get(ArexConstants.REPLAY_WARM_UP)).thenReturn("true");
        };
        Runnable mocker4 = () -> {
            Mockito.when(headers.get(ArexConstants.REPLAY_WARM_UP)).thenReturn("false");
        };
        Runnable mocker4_1 = () -> {
            Mockito.when(IgnoreUtils.ignoreOperation(any())).thenReturn(true);
        };
        Runnable mocker5 = () -> {
            Mockito.when(IgnoreUtils.ignoreOperation(any())).thenReturn(false);
            Mockito.when(RecordLimiter.acquire(any())).thenReturn(true);
        };
        Channel channel = Mockito.mock(Channel.class);
        Attribute attribute = Mockito.mock(Attribute.class);
        Runnable mocker6 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ctx.channel()).thenReturn(channel);
            Mockito.when(channel.attr(any())).thenReturn(attribute);
        };
        Runnable mocker7 = () -> {
            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            Mockito.when(attribute.get()).thenReturn(mocker);
            Mockito.when(NettyHelper.parseBody(any())).thenReturn("mock");
        };

        LastHttpContent content = Mockito.mock(LastHttpContent.class);

        return Stream.of(
                arguments(mocker1, request),
                arguments(mocker2, request),
                arguments(mocker3, request),
                arguments(mocker4, request),
                arguments(mocker4_1, request),
                arguments(mocker5, request),
                arguments(mocker6, request),
                arguments(mocker7, content)
        );
    }
}