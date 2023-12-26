package io.arex.inst.lettuce.v6.cluster.inst;

import static net.bytebuddy.matcher.ElementMatchers.named;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.lettuce.v6.cluster.RedisClusterAsyncCommandsImplWrapper;
import io.arex.inst.lettuce.v6.cluster.RedisClusterReactiveCommandsImplWrapper;
import io.lettuce.core.cluster.StatefulRedisClusterConnectionImpl;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands;
import io.lettuce.core.codec.RedisCodec;
import java.util.Arrays;
import java.util.List;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * StatefulRedisClusterConnectionImplInstrumentation
 */
public class StatefulRedisClusterConnectionImplInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("io.lettuce.core.cluster.StatefulRedisClusterConnectionImpl");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> asyncMatcher = named("async");

        ElementMatcher<MethodDescription> reactiveMatcher = named("reactive");

        return Arrays.asList(
            new MethodInstrumentation(asyncMatcher, NewRedisAsyncCommandsImplAdvice.class.getName()),
            new MethodInstrumentation(reactiveMatcher, NewRedisReactiveCommandsImplAdvice.class.getName()));
    }

    public static class NewRedisAsyncCommandsImplAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <K, V> void onExit(@Advice.This StatefulRedisClusterConnectionImpl<K, V> connection,
            @Advice.FieldValue("codec") RedisCodec<K, V> codec,
            @Advice.Return(readOnly = false) RedisAdvancedClusterAsyncCommands<K, V> returnValue) {
            returnValue = new RedisClusterAsyncCommandsImplWrapper<>(connection, codec);
        }
    }

    public static class NewRedisReactiveCommandsImplAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <K, V> void onExit(@Advice.This StatefulRedisClusterConnectionImpl<K, V> connection,
            @Advice.FieldValue("codec") RedisCodec<K, V> codec,
            @Advice.Return(readOnly = false) RedisAdvancedClusterReactiveCommands<K, V> returnValue) {
            returnValue = new RedisClusterReactiveCommandsImplWrapper<>(connection, codec);
        }
    }
}
