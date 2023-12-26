package io.arex.inst.redis.common.lettuce;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.common.util.FluxUtil;
import io.arex.inst.redis.common.RedisExtractor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public  class RedisClusterReactiveResultUtil {

    private RedisClusterReactiveResultUtil() {
    }

    public static Mono<?> monoRecord(String redisUri,Mono<?> monoResult,String methodName,String key,String field) {
        RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, field);
        return new MonoConsumer(extractor).accept(monoResult);
    }

    public static Mono<?> monoReplay(String redisUri,String methodName,String key,String field) {
        RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, field);
        MockResult mockResult = extractor.replay();
        if (mockResult.notIgnoreMockResult()) {
            if (mockResult.getThrowable() != null) {
                return Mono.error(mockResult.getThrowable());
            }
            return Mono.justOrEmpty(mockResult.getResult());
        }
        return Mono.empty();
    }

    public static Flux<?> fluxRecord(String redisUri,Flux<?> fluxResult,String methodName,String key,String field) {
        RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, field);
        return new FluxConsumer(extractor).accept(fluxResult);
    }

    public static Flux<?> fluxReplay(String redisUri,String methodName,String key,String field) {
        RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, field);
        MockResult mockResult = extractor.replay();
        if (mockResult.notIgnoreMockResult()) {
            if (mockResult.getThrowable() != null) {
                return Flux.error(mockResult.getThrowable());
            }
            return FluxUtil.restore(mockResult.getResult());
        }
        return Flux.empty();
    }
}
