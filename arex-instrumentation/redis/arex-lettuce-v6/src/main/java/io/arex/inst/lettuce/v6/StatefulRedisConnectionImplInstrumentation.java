package io.arex.inst.lettuce.v6;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.lettuce.core.RedisAsyncCommandsImpl;
import io.lettuce.core.RedisReactiveCommandsImpl;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.codec.RedisCodec;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
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

        String asyncAdvice = this.getClass().getName() + "$NewRedisAsyncCommandsImplAdvice";

        ElementMatcher<MethodDescription> reactiveMatcher = isProtected().and(named("newRedisReactiveCommandsImpl"));

        String reactiveAdvice = this.getClass().getName() + "$NewRedisReactiveCommandsImplAdvice";

        return Arrays.asList(new MethodInstrumentation(asyncMatcher, asyncAdvice),
            new MethodInstrumentation(reactiveMatcher, reactiveAdvice));
    }


    @Override
    public List<String> adviceClassNames() {
        return asList(
                "io.arex.inst.lettuce.v6.StatefulRedisConnectionImplInstrumentation$NewRedisAsyncCommandsImplAdvice",
                "io.arex.inst.lettuce.v6.StatefulRedisConnectionImplInstrumentation$NewRedisReactiveCommandsImplAdvice");
    }

    public static class NewRedisAsyncCommandsImplAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class)
        public static boolean onEnter() {
            return false;
        }

        @Advice.OnMethodExit
        public static <K, V> void onExit(@Advice.This StatefulRedisConnectionImpl<K, V> connection,
            @Advice.FieldValue("codec") RedisCodec<K, V> codec,
            @Advice.Return(readOnly = false) RedisAsyncCommandsImpl<K, V> returnValue) {
            returnValue = new RedisAsyncCommandsImplWrapper<>(connection, codec);
        }
    }

    public static class NewRedisReactiveCommandsImplAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class)
        public static boolean onEnter() {
            return false;
        }

        @Advice.OnMethodExit
        public static <K, V> void onExit(@Advice.This StatefulRedisConnectionImpl<K, V> connection,
            @Advice.FieldValue("codec") RedisCodec<K, V> codec,
            @Advice.Return(readOnly = false) RedisReactiveCommandsImpl<K, V> returnValue) {
            returnValue = new RedisReactiveCommandsImplWrapper<>(connection, codec);
        }
    }
}
