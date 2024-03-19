package io.arex.inst.reactorcore;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;
import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.inst.reactorcore.common.MonoToFutureWrapper;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.Local;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * MonoInstrumentation
 */
public class MonoInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("reactor.core.publisher.Mono");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("toFuture").and(isPublic()).and(takesNoArguments());

        return asList(
            new MethodInstrumentation(matcher, MonoToFutureAdvice.class.getName())
        );
    }

    public static class MonoToFutureAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Local("tm") TraceTransmitter tm) {
            tm = TraceTransmitter.create();
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <T> void onExit(@Local("tm") TraceTransmitter tm,
            @Advice.Return(readOnly = false) CompletableFuture<T> result) {
            result = MonoToFutureWrapper.thenApplyAsync(result, tm);
        }
    }
}
