package io.arex.inst.redis.common.lettuce;

import io.arex.agent.bootstrap.ctx.TraceTransmitter;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.protocol.AsyncCommand;
import io.lettuce.core.protocol.Command;

public class LettuceClusterUtil {

    private LettuceClusterUtil(){}

    public static <K, V, R> RedisFuture<R> clusterAsynReplay(String methodName, String key, Command<K, V, R> cmd,
        String redisUri) {
        RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, null);
        MockResult mockResult = extractor.replay();
        AsyncCommand<K, V, R> asyncCommand = new AsyncCommand<>(cmd);
        if (mockResult.notIgnoreMockResult()) {
            if (mockResult.getThrowable() != null) {
                asyncCommand.completeExceptionally(mockResult.getThrowable());
            } else {
                asyncCommand.complete((R) mockResult.getResult());
            }
        }
        return asyncCommand;
    }

    public static void clusterAsyncRecord(String key, RedisFuture<?> resultFuture, String methodName, String redisUri) {
        try (TraceTransmitter traceTransmitter = TraceTransmitter.create()) {
            resultFuture.whenComplete((v, throwable) -> {
                RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, null);
                traceTransmitter.transmit();
                if (throwable != null) {
                    extractor.record(throwable);
                } else {
                    extractor.record(v);
                }
            });
        }
    }
}
