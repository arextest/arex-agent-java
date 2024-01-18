package io.arex.inst.lettuce.v6.cluster.inst;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.redis.common.RedisConnectionManager;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * RedisClusterClientInstrumentation
 */
public class RedisClusterClientInstrumentation extends TypeInstrumentation {

    @Override
    protected ElementMatcher<TypeDescription> typeMatcher() {
        return named("io.lettuce.core.cluster.RedisClusterClient");
    }

    @Override
    public List<MethodInstrumentation> methodAdvices() {
        ElementMatcher<MethodDescription> matcher = namedOneOf("connectAsync", "connectClusterAsync");

        return Collections.singletonList(
            new MethodInstrumentation(matcher, NewStatefulRedisConnectionAdvice.class.getName()));
    }


    public static class NewStatefulRedisConnectionAdvice {

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static <K, V> void onExit(
            @Advice.Return(readOnly = false) CompletableFuture<StatefulRedisClusterConnection<K, V>> connectionFuture,
            @Advice.FieldValue("initialUris") Iterable<RedisURI> redisURIs) {
            RedisConnectionManager.addClusterConnection(connectionFuture, redisURIs);
        }
    }
}
