package io.arex.inst.netty.v3;

import io.arex.agent.bootstrap.internal.CallDepth;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
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
            ChannelPipelineHelper.addHandler(pipeline, handlerName, handler, callDepth);
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
            ChannelPipelineHelper.addHandler(pipeline, handlerName, handler, callDepth);
        }
    }
}
