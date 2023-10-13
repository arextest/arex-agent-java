package io.arex.inst.netty.v3;

import io.arex.agent.bootstrap.internal.CallDepth;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.netty.v3.server.RequestTracingHandler;
import io.arex.inst.netty.v3.server.ResponseTracingHandler;
import io.arex.inst.netty.v3.server.ServerCodecTracingHandler;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpServerCodec;

import java.util.List;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class ChannelPipelineInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.jboss.netty.channel.DefaultChannelPipeline");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        MethodInstrumentation twoParamMethod = new MethodInstrumentation(
                isMethod().and(nameStartsWith("add").or(named("replace")))
                        .and(takesArgument(0, String.class))
                        .and(takesArgument(1, named("org.jboss.netty.channel.ChannelHandler"))),
                AddHandlerAdviceWithTwoParam.class.getName());

        MethodInstrumentation threeParamMethod = new MethodInstrumentation(
                isMethod().and(nameStartsWith("add").or(named("replace")))
                        .and(takesArgument(1, String.class))
                        .and(takesArgument(2, named("org.jboss.netty.channel.ChannelHandler"))),
                AddHandlerAdviceWithThreeParam.class.getName());

        return asList(twoParamMethod, threeParamMethod);
    }

    public static final class AddHandlerAdviceWithTwoParam {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(1) ChannelHandler handler,
                                   @Advice.Local("callDepth") CallDepth callDepth) {
            callDepth = CallDepth.forClass(handler.getClass());
            callDepth.getAndIncrement();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.This ChannelPipeline pipeline,
                                  @Advice.Argument(0) String handlerName,
                                  @Advice.Argument(1) ChannelHandler handler,
                                  @Advice.Local("callDepth") CallDepth callDepth) {
            if (callDepth.decrementAndGet() > 0 || handler.getClass().getName().startsWith("io.arex.inst")) {
                return;
            }

            String name = handlerName;
            if (name == null) {
                ChannelHandlerContext context = pipeline.getContext(handler);
                if (context == null) {
                    return;
                }
                name = context.getName();
            }
            if (handler instanceof HttpRequestDecoder) {
                pipeline.addAfter(name, "io.arex.inst.netty.v3.server.RequestTracingHandler",
                        new RequestTracingHandler());
            } else if (handler instanceof HttpResponseEncoder) {
                pipeline.addAfter(name, "io.arex.inst.netty.v3.server.ResponseTracingHandler",
                        new ResponseTracingHandler());
            } else if (handler instanceof HttpServerCodec) {
                pipeline.addAfter(name, "io.arex.inst.netty.v3.server.ServerCodecTracingHandler",
                        new ServerCodecTracingHandler());
            }
        }
    }

    public static final class AddHandlerAdviceWithThreeParam {

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
                ChannelHandlerContext context = pipeline.getContext(handler);
                if (context == null) {
                    return;
                }
                name = context.getName();
            }
            if (handler instanceof HttpRequestDecoder) {
                pipeline.addAfter(name, "io.arex.inst.netty.v3.server.RequestTracingHandler",
                        new RequestTracingHandler());
            } else if (handler instanceof HttpResponseEncoder) {
                pipeline.addAfter(name, "io.arex.inst.netty.v3.server.ResponseTracingHandler",
                        new ResponseTracingHandler());
            } else if (handler instanceof HttpServerCodec) {
                pipeline.addAfter(name, "io.arex.inst.netty.v3.server.ServerCodecTracingHandler",
                        new ServerCodecTracingHandler());
            }
        }
    }
}
