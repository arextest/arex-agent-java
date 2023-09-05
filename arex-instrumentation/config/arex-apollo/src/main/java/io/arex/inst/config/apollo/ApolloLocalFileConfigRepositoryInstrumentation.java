package io.arex.inst.config.apollo;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * ApolloLocalFileConfigRepositoryInstrumentation
 */
public class ApolloLocalFileConfigRepositoryInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.ctrip.framework.apollo.internals.LocalFileConfigRepository");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("persistLocalCacheFile").and(takesArguments(2));
        return singletonList(new MethodInstrumentation(matcher, PersistAdvice.class.getName()));
    }

    public static class PersistAdvice {

        /**
         * not store config to local file during replay
         */
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return ApolloConfigExtractor.duringReplay();
        }
    }
}
