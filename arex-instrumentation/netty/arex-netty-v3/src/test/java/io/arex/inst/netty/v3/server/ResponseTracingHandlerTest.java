package io.arex.inst.netty.v3.server;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.netty.v3.common.NettyHelper;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponse;
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
    static MockedStatic<CaseEventDispatcher> mockCaseEvent;
    static HttpResponse response;

    @BeforeAll
    static void setUp() {
        target = new ResponseTracingHandler();
        ctx = Mockito.mock(ChannelHandlerContext.class);
        response = Mockito.mock(HttpResponse.class);
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
        response = null;
        mockCaseEvent = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("writeRequestedCase")
    void writeRequested(Runnable mocker, MessageEvent event) throws Exception {
        mocker.run();
        target.writeRequested(ctx, event);
        verify(ctx, atLeastOnce()).sendDownstream(any());
    }

    static Stream<Arguments> writeRequestedCase() {
        Runnable emptyMocker = () -> {};
        MessageEvent event = Mockito.mock(MessageEvent.class);
        ArexContext context = Mockito.mock(ArexContext.class);
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(event.getMessage()).thenReturn(response);
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
            Mockito.when(NettyHelper.parseBody(any())).thenReturn("mock");
        };
        ArexMocker mocker = new ArexMocker();
        Runnable mocker2 = () -> {
            mocker.setTargetRequest(new Mocker.Target());
            mocker.setTargetResponse(new Mocker.Target());
            Mockito.when(context.getAttachment(any())).thenReturn(mocker);
        };
        Runnable mocker3 = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
        };
        Runnable mocker4 = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        return Stream.of(
                arguments(emptyMocker, event),
                arguments(mocker1, event),
                arguments(mocker2, event),
                arguments(mocker3, event),
                arguments(mocker4, event)
        );
    }
}