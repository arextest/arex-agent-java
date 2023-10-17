package io.arex.inst.netty.v4;

import io.arex.agent.bootstrap.internal.CallDepth;
import io.arex.agent.bootstrap.util.Assert;
import io.arex.inst.runtime.context.ContextManager;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChannelPipelineInstrumentationTest {
    static ChannelPipelineInstrumentation target = null;
    @BeforeAll
    static void setUp() {
        target = new ChannelPipelineInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.mockStatic(CallDepth.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @Test
    void onEnter() {
        ChannelHandler handler = Mockito.mock(ChannelHandler.class);
        CallDepth callDepth = Mockito.mock(CallDepth.class);
        Mockito.when(CallDepth.forClass(any(Class.class))).thenReturn(callDepth);
        ChannelPipelineInstrumentation.AddHandlerAdvice.onEnter(handler, callDepth);
        verify(callDepth, atLeastOnce()).getAndIncrement();
    }

    @ParameterizedTest
    @MethodSource("onExitCase")
    void onExit(Runnable mocker, ChannelPipeline pipeline, ChannelHandler handler, CallDepth callDepth, Assert asserts) {
        mocker.run();
        ChannelPipelineInstrumentation.AddHandlerAdvice.onExit(pipeline, null, handler, callDepth);
        asserts.verity();
    }

    static Stream<Arguments> onExitCase() {
        Runnable emptyMocker = () -> {};
        CallDepth callDepth = Mockito.mock(CallDepth.class);
        Runnable mocker1 = () -> {
            Mockito.when(callDepth.decrementAndGet()).thenReturn(1);
        };
        ChannelPipeline pipeline = Mockito.mock(ChannelPipeline.class);
        Runnable mocker2 = () -> {
            Mockito.when(callDepth.decrementAndGet()).thenReturn(0);
        };
        Runnable mocker3 = () -> {
            ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
            Mockito.when(pipeline.context(any(ChannelHandler.class))).thenReturn(context);
        };
        ChannelHandler handler1 = Mockito.mock(HttpRequestDecoder.class);
        ChannelHandler handler2 = Mockito.mock(HttpResponseEncoder.class);
        ChannelHandler handler3 = Mockito.mock(HttpServerCodec.class);
        Assert addAfter = () -> {
            verify(pipeline, atLeastOnce()).addAfter(any(), any(), any());
        };
        Assert notAddAfter = () -> {
            verify(pipeline, never()).addAfter(any(), any(), any());
        };
        return Stream.of(
                arguments(mocker1, pipeline, handler1, callDepth, notAddAfter),
                arguments(mocker2, pipeline, handler1, callDepth, notAddAfter),
                arguments(mocker3, pipeline, handler1, callDepth, addAfter),
                arguments(emptyMocker, pipeline, handler2, callDepth, addAfter),
                arguments(emptyMocker, pipeline, handler3, callDepth, addAfter)
        );
    }
}