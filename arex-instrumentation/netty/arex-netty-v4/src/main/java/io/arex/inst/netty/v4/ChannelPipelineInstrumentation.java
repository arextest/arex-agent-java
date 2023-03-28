package io.arex.inst.netty.v4;

import io.arex.agent.bootstrap.internal.CallDepth;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.netty.v4.server.RequestTracingHandler;
import io.arex.inst.netty.v4.server.ResponseTracingHandler;
import io.arex.inst.netty.v4.server.ServerCodecTracingHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class ChannelPipelineInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("io.netty.channel.DefaultChannelPipeline");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        return singletonList(new MethodInstrumentation(
                isMethod().and(nameStartsWith("add").or(named("replace")))
                        .and(takesArgument(1, String.class))
                        .and(takesArgument(2, named("io.netty.channel.ChannelHandler"))),
                AddHandlerAdvice.class.getName()));
    }

    public static final class AddHandlerAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(2) ChannelHandler handler,
                                   @Advice.Local("callDepth") CallDepth callDepth) {
            callDepth = CallDepth.forClass(handler.getClass());
            callDepth.getAndIncrement();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This ChannelPipeline pipeline,
                                  @Advice.Argument(1) String handlerName,
                                  @Advice.Argument(2) ChannelHandler handler,
                                  @Advice.Local("callDepth") CallDepth callDepth) {
            if (callDepth.decrementAndGet() > 0 || handler.getClass().getName().startsWith("io.arex.inst")) {
                return;
            }

            String name = handlerName;
            if (name == null) {
                ChannelHandlerContext context = pipeline.context(handler);
                if (context == null) {
                    return;
                }
                name = context.name();
            }

            if (handler instanceof HttpRequestDecoder) {
                pipeline.addAfter(name, "io.arex.inst.netty.v4.server.RequestTracingHandler",
                        new RequestTracingHandler());
            } else if (handler instanceof HttpResponseEncoder) {
                pipeline.addAfter(name, "io.arex.inst.netty.v4.server.ResponseTracingHandler",
                        new ResponseTracingHandler());
            } else if (handler instanceof HttpServerCodec) {
                pipeline.addAfter(name, "io.arex.inst.netty.v4.server.ServerCodecTracingHandler",
                        new ServerCodecTracingHandler());
            }
        }
    }
}
