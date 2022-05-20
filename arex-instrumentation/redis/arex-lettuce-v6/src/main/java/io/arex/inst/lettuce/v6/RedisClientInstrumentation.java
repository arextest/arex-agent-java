package io.arex.inst.lettuce.v6;

import io.arex.foundation.api.MethodInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.lettuce.core.RedisURI;
import io.lettuce.core.StatefulRedisConnectionImpl;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collections;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isProtected;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * RedisClientInstrumentation
 */
public class RedisClientInstrumentation extends TypeInstrumentation {
    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("io.lettuce.core.RedisClient");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = isProtected().and(named("newStatefulRedisConnection"));

        String adviceClassName = this.getClass().getName() + "$NewStatefulRedisConnectionAdvice";

        return Collections.singletonList(new MethodInstrumentation(matcher, adviceClassName));
    }

    public static class NewStatefulRedisConnectionAdvice {
        @Advice.OnMethodExit
        public static <K, V> void onExit(
            @Advice.Return(readOnly = false) StatefulRedisConnectionImpl<K, V> connection,
            @Advice.FieldValue("redisURI") RedisURI redisURI) {
            LettuceHelper.putToUriMap(connection.hashCode(), redisURI);
        }
    }
}
