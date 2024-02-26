package io.arex.inst.redisson.v3;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redisson.v3.common.RFutureWrapper;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.redis.common.RedisExtractor;
import org.redisson.api.RFuture;

import java.util.concurrent.Callable;

/**
 * RedissonWrapperCommon
 */
public class RedissonWrapperCommon {
    public static <R> RFuture<R> delegateCall(String redisUri, String cmd, String key,
                                              Callable<RFuture<R>> resultFuture) {
        return delegateCall(redisUri, cmd, key, null, resultFuture);
    }

    public static <R> RFuture<R> delegateCall(String redisUri, String cmd, String key, String field,
                                              Callable<RFuture<R>> futureCallable) {
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

        RFuture<R> resultFuture = null;
        try {
            resultFuture = futureCallable.call();
        } catch (Exception e) {
            // The following codes may not execute, just catch checked exception
            if (ContextManager.needRecord()) {
                RedisExtractor extractor = new RedisExtractor(redisUri, cmd, key, field);
                extractor.record(e);
            }

            return resultFuture;
        }

        if (resultFuture != null && ContextManager.needRecord()) {
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
