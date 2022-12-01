package io.arex.inst.netty.v4.server;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.listener.CaseInitializer;
import io.arex.foundation.model.Constants;
import io.arex.foundation.model.ServiceEntranceMocker;
import io.arex.foundation.services.IgnoreService;
import io.arex.inst.netty.v4.common.NettyHelper;
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
import static org.mockito.ArgumentMatchers.eq;
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
        Mockito.mockStatic(CaseInitializer.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(IgnoreService.class);
        Mockito.mockStatic(NettyHelper.class);
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
            Mockito.when(headers.get(eq(Constants.RECORD_ID))).thenReturn("mock");
        };
        Runnable mocker2 = () -> {
            Mockito.when(headers.get(eq(Constants.RECORD_ID))).thenReturn("");
            Mockito.when(headers.get(eq(Constants.FORCE_RECORD))).thenReturn("true");
            Mockito.when(request.method()).thenReturn(HttpMethod.POST);
        };
        Runnable mocker3 = () -> {
            Mockito.when(headers.get(eq(Constants.FORCE_RECORD))).thenReturn("false");
            Mockito.when(headers.get(eq(Constants.REPLAY_WARM_UP))).thenReturn("true");
        };
        Runnable mocker4 = () -> {
            Mockito.when(headers.get(eq(Constants.REPLAY_WARM_UP))).thenReturn("false");
            Mockito.when(CaseInitializer.exceedRecordRate(any())).thenReturn(true);
        };
        Runnable mocker5 = () -> {
            Mockito.when(CaseInitializer.exceedRecordRate(any())).thenReturn(false);
            Mockito.when(IgnoreService.isServiceEnabled(any())).thenReturn(true);
        };
        Channel channel = Mockito.mock(Channel.class);
        Attribute attribute = Mockito.mock(Attribute.class);
        Runnable mocker6 = () -> {
            Mockito.when(IgnoreService.isServiceEnabled(any())).thenReturn(false);
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ctx.channel()).thenReturn(channel);
            Mockito.when(channel.attr(any())).thenReturn(attribute);
        };
        Runnable mocker7 = () -> {
            Mockito.when(attribute.get()).thenReturn(Mockito.mock(ServiceEntranceMocker.class));
            Mockito.when(NettyHelper.parseBody(any())).thenReturn("mock");
        };

        LastHttpContent content = Mockito.mock(LastHttpContent.class);

        return Stream.of(
                arguments(mocker1, request),
                arguments(mocker2, request),
                arguments(mocker3, request),
                arguments(mocker4, request),
                arguments(mocker5, request),
                arguments(mocker6, request),
                arguments(mocker7, content)
        );
    }
}