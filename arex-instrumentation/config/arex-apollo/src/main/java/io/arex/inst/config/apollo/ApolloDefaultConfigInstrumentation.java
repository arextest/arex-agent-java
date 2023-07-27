package io.arex.inst.config.apollo;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * ApolloDefaultConfigInstrumentation
 */
public class ApolloDefaultConfigInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.ctrip.framework.apollo.internals.DefaultConfig");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        // compatible with different versions of Apollo
        ElementMatcher<MethodDescription> matcher = named("updateAndCalcConfigChanges")
                .and(takesArgument(0, named("java.util.Properties")));

        return singletonList(new MethodInstrumentation(matcher, UpdateAdvice.class.getName()));
    }

    public static class UpdateAdvice {

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit() {
            /*
            Executing this method means that the configuration has changed.
            Because the Apollo configuration is divided by namespace,
            as long as there is a configuration change in one namespace,
            the configuration in all namespaces will be recorded on next request.
             */
            ApolloConfigExtractor.onConfigUpdate();
        }
    }
}
