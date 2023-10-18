package io.arex.inst.netty.v3;

import io.arex.agent.bootstrap.internal.CallDepth;
import io.arex.inst.runtime.context.ContextManager;
import org.jboss.netty.channel.ChannelHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
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
        Mockito.mockStatic(ChannelPipelineHelper.class);
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
        ChannelPipelineInstrumentation.AddHandlerAdviceWithTwoParam.onEnter(handler, callDepth);
        verify(callDepth, atLeastOnce()).getAndIncrement();
    }

    @Test
    void onExit() {
        assertDoesNotThrow(() -> ChannelPipelineInstrumentation.AddHandlerAdviceWithTwoParam.onExit(
                null, null, null, null));
    }

    @Test
    void onEnterTest() {
        ChannelHandler handler = Mockito.mock(ChannelHandler.class);
        CallDepth callDepth = Mockito.mock(CallDepth.class);
        Mockito.when(CallDepth.forClass(any(Class.class))).thenReturn(callDepth);
        ChannelPipelineInstrumentation.AddHandlerAdviceWithThreeParam.onEnter(handler, callDepth);
        verify(callDepth, atLeastOnce()).getAndIncrement();
    }

    @Test
    void onExitTest() {
        assertDoesNotThrow(() -> ChannelPipelineInstrumentation.AddHandlerAdviceWithThreeParam.onExit(
                null, null, null, null));
    }
}