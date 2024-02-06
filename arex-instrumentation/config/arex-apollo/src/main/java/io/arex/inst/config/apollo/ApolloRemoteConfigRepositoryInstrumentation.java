package io.arex.inst.config.apollo;

import com.ctrip.framework.apollo.core.dto.ApolloConfig;
import com.ctrip.framework.apollo.util.ConfigUtil;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * ApolloRemoteConfigRepositoryInstrumentation
 */
public class ApolloRemoteConfigRepositoryInstrumentation extends TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.ctrip.framework.apollo.internals.RemoteConfigRepository");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = named("loadApolloConfig").and(takesNoArguments());
        return singletonList(new MethodInstrumentation(matcher, LoadAdvice.class.getName()));
    }

    public static class LoadAdvice {

        /**
         * <pre>
         * called by
         * io.arex.inst.config.apollo.ApolloConfigHelper#replayAllConfigs
         * \-- com.ctrip.framework.apollo.internals.RemoteConfigRepository.sync
         */
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static ApolloConfig onEnter(
                @Advice.FieldValue("mConfigCache") AtomicReference<ApolloConfig> configCache,
                @Advice.FieldValue("mNamespace") String namespace,
                @Advice.FieldValue("mConfigUtil") ConfigUtil configUtil) {
            return ApolloConfigHelper.getReplayConfig(configCache.get(), namespace, configUtil);
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Enter ApolloConfig config,
                                  @Advice.Return(readOnly = false) ApolloConfig result) {
            if (config != null) {
                result = config;
            }
        }
    }
}
