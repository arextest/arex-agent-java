package io.arex.inst.redis.common.lettuce;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.reactorcore.common.FluxRecordFunction;
import io.arex.inst.reactorcore.common.FluxReplayUtil;
import io.arex.inst.reactorcore.common.FluxReplayUtil.FluxResult;
import io.arex.inst.reactorcore.common.MonoRecordFunction;
import io.arex.inst.redis.common.RedisExtractor;
import java.util.function.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public  class ReactorStreamUtil {

    private ReactorStreamUtil() {
    }

    public static Mono<?> monoRecord(String redisUri,Mono<?> monoResult,String methodName,String key,String field) {
        RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, field);
        Function<Object,Void> executor = result -> {
            extractor.record(result);
            return null;
        };
        return new MonoRecordFunction(executor).apply(monoResult);
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
        Function<FluxResult,Void> executor = result -> {
            extractor.record(result);
            return null;
        };
        return new FluxRecordFunction(executor).apply(fluxResult);
    }

    public static Flux<?> fluxReplay(String redisUri,String methodName,String key,String field) {
        RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, field);
        MockResult mockResult = extractor.replay();
        if (mockResult.notIgnoreMockResult()) {
            if (mockResult.getThrowable() != null) {
                return Flux.error(mockResult.getThrowable());
            }
            return FluxReplayUtil.restore(mockResult.getResult());
        }
        return Flux.empty();
    }
}
