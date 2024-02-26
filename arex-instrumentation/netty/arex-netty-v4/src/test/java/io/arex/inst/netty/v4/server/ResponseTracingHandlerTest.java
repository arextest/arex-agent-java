package io.arex.inst.netty.v4.server;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.netty.v4.common.NettyHelper;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResponseTracingHandlerTest {
    static ResponseTracingHandler target;
    static ChannelHandlerContext ctx;
    static HttpHeaders headers;
    static MockedStatic<CaseEventDispatcher> mockCaseEvent;

    @BeforeAll
    static void setUp() {
        target = new ResponseTracingHandler();
        ctx = Mockito.mock(ChannelHandlerContext.class);
        headers = Mockito.mock(HttpHeaders.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(IgnoreUtils.class);
        Mockito.mockStatic(NettyHelper.class);
        Mockito.mockStatic(RecordLimiter.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
        mockCaseEvent = Mockito.mockStatic(CaseEventDispatcher.class);
        Mockito.mockStatic(MockUtils.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        ctx = null;
        headers = null;
        mockCaseEvent = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("writeCase")
    void write(Runnable mocker, Object msg) throws Exception {
        mocker.run();
        ChannelPromise promise = Mockito.mock(ChannelPromise.class);
        target.write(ctx, msg, promise);
        verify(ctx, atLeastOnce()).write(any(), any());
    }

    static Stream<Arguments> writeCase() {
        Runnable emptyMocker = () -> {};
        Object msg = Mockito.mock(FullHttpResponse.class);
        Channel channel = Mockito.mock(Channel.class);
        Attribute attribute = Mockito.mock(Attribute.class);
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetRequest(new Mocker.Target());
        mocker.setTargetResponse(new Mocker.Target());
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ctx.channel()).thenReturn(channel);
            Mockito.when(channel.attr(any())).thenReturn(attribute);
            Mockito.when(attribute.get()).thenReturn(mocker);
            Mockito.when(NettyHelper.parseBody(any())).thenReturn("mock");
        };

        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        Runnable mocker3 = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Object msg1 = Mockito.mock(HttpResponse.class);
        return Stream.of(
                arguments(emptyMocker, msg),
                arguments(mocker1, msg),
                arguments(mocker2, msg),
                arguments(mocker3, msg),
                arguments(emptyMocker, msg1)
        );
    }
}