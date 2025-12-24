package io.arex.inst.redisson.v3;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redisson.v3.common.RFutureWrapper;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import java.util.function.Supplier;
import org.redisson.api.RFuture;

/**
 * RedissonWrapperCommon
 */
public class RedissonWrapperCommon {
    public static <R> RFuture<R> delegateCall(String redisUri, String cmd, String key,
        Supplier<RFuture<R>> futureSupplier) {
        return delegateCall(redisUri, cmd, key, null, futureSupplier);
    }

    public static <R> RFuture<R> delegateCall(String redisUri, String cmd, String key, String field,
        Supplier<RFuture<R>> futureSupplier) {
        if (ContextManager.needRecord()) {
            RepeatedCollectManager.enter();
        }
        if (ContextManager.needReplay()) {
            RedisExtractor extractor = new RedisExtractor(redisUri, cmd, key, field);
            MockResult mockResult = extractor.replay();
            if (mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    return new RFutureWrapper<>(mockResult.getThrowable());
                }
                return new RFutureWrapper<>((R) mockResult.getResult());
            }
        }

        RFuture<R> resultFuture = futureSupplier.get();

        if (resultFuture != null && ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate("redis repeat record")) {
            try (TraceTransmitter traceTransmitter = TraceTransmitter.create()) {
                resultFuture.whenComplete((v, throwable) -> {
                    traceTransmitter.transmit();
                    RedisExtractor extractor = new RedisExtractor(redisUri, cmd, key, field);
                    if (throwable != null) {
                        extractor.record(throwable);
                    } else {
                        extractor.record(v);
                    }
                });
            }
        }

        return resultFuture;
    }
}
