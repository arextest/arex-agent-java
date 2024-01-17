package io.arex.inst.lettuce.v5.standalone.inst;

import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.redis.common.RedisConnectionManager;
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

        return Collections.singletonList(
            new MethodInstrumentation(matcher, NewStatefulRedisConnectionAdvice.class.getName()));
    }


    public static class NewStatefulRedisConnectionAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <K, V> void onExit(
                @Advice.Return(readOnly = false) StatefulRedisConnectionImpl<K, V> connection,
                @Advice.FieldValue("redisURI") RedisURI redisURI) {
            RedisConnectionManager.add(connection.hashCode(), redisURI.toString());
        }
    }
}
