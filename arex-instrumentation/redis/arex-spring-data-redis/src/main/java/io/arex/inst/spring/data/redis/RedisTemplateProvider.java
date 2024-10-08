package io.arex.inst.spring.data.redis;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.redis.common.RedisExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;

/**
 * RedisTemplateProvider
 */
public class RedisTemplateProvider {

    public static void methodOnExit(String redisUri, String methodName, Object key, Object result,
        Throwable throwable) {
        if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate("redis repeat record")) {
            RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, null);
            if (throwable != null) {
                extractor.record(throwable);
            } else {
                extractor.record(result);
            }
        }
    }

    public static MockResult methodOnEnter(String redisUri, String methodName, Object key) {
        if (ContextManager.needRecord()) {
            RepeatedCollectManager.enter();
            return null;
        }
        if (ContextManager.needReplay()) {
            RedisExtractor extractor = new RedisExtractor(redisUri, methodName, key, null);
            return extractor.replay();
        }
        return null;
    }
}
