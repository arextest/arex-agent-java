package io.arex.inst.lettuce.v6;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.lettuce.core.RedisAsyncCommandsImpl;
import io.lettuce.core.RedisReactiveCommandsImpl;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.codec.RedisCodec;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isProtected;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * StatefulRedisConnectionImplInstrumentation
 */
public class StatefulRedisConnectionImplInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("io.lettuce.core.StatefulRedisConnectionImpl");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> asyncMatcher = isProtected().and(named("newRedisAsyncCommandsImpl"));

        ElementMatcher<MethodDescription> reactiveMatcher = isProtected().and(named("newRedisReactiveCommandsImpl"));

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
        public static <K, V> void onExit(@Advice.This StatefulRedisConnectionImpl<K, V> connection,
            @Advice.FieldValue("codec") RedisCodec<K, V> codec,
            @Advice.Return(readOnly = false) RedisAsyncCommandsImpl<K, V> returnValue) {
            returnValue = new RedisAsyncCommandsImplWrapper<>(connection, codec);
        }
    }

    public static class NewRedisReactiveCommandsImplAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class, suppress = Throwable.class)
        public static boolean onEnter() {
            return true;
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <K, V> void onExit(@Advice.This StatefulRedisConnectionImpl<K, V> connection,
            @Advice.FieldValue("codec") RedisCodec<K, V> codec,
            @Advice.Return(readOnly = false) RedisReactiveCommandsImpl<K, V> returnValue) {
            returnValue = new RedisReactiveCommandsImplWrapper<>(connection, codec);
        }
    }
}
