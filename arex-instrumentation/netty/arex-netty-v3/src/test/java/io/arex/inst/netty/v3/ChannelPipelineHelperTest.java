package io.arex.inst.netty.v3;

import io.arex.agent.bootstrap.internal.CallDepth;
import io.arex.agent.bootstrap.util.Assert;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpServerCodec;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChannelPipelineHelperTest {

    @ParameterizedTest
    @MethodSource("addHandlerCase")
    void addHandler(Runnable mocker, ChannelPipeline pipeline, ChannelHandler handler, CallDepth callDepth, Assert asserts) {
        mocker.run();
        ChannelPipelineHelper.addHandler(pipeline, null, handler, callDepth);
        asserts.verity();
    }

    static Stream<Arguments> addHandlerCase() {
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
            Mockito.when(pipeline.getContext(any(ChannelHandler.class))).thenReturn(context);
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