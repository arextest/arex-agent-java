package io.arex.inst.cache.common;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.cache.guava.FunctionWithException;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.log.LogManager;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class CacheWrapperCommon {
    public static <K, V> V process(String className, String methodName, K key, String methodReturnType, Function<K, V> function) {
        try {
            return processInternal(className, methodName, key, methodReturnType, function, false);
        } catch (ExecutionException e) {
            return function.apply(key);
        }
    }

    public static <K, V> V processWithException(String className, String methodName, K key, String methodReturnType, FunctionWithException<K, V> function) throws ExecutionException {
        return processInternal(className, methodName, key, methodReturnType, function, true);
    }

    private static <K, V> V processInternal(String className, String methodName, K key, String methodReturnType, Object function, boolean withException) throws ExecutionException {
        try {
            if (ContextManager.needReplay()) {
                return (V) replay(className, methodName, key, methodReturnType);
            }

            if (ContextManager.needRecord()) {
                RepeatedCollectManager.enter();
            }
        } catch (Throwable ex) {
            LogManager.warn("CacheCommonWrapper.process.replay", ex);
        }

        V response;
        if (withException) {
            response = ((FunctionWithException<K, V>) function).apply(key);
        } else {
            response = ((Function<K, V>)function).apply(key);
        }

        try {
            record(className, methodName, key, methodReturnType, response);
        } catch (Throwable ex) {
            LogManager.warn("CacheCommonWrapper.process.record", ex);
        }
        return response;
    }

    private static void record(String className, String methodName, Object key, String methodReturnType, Object response) {
        if (ContextManager.needRecord() && RepeatedCollectManager.exitAndValidate()) {
            DynamicClassExtractor extractor = new DynamicClassExtractor(className, methodName, new Object[]{key}, methodReturnType);
            extractor.recordResponse(response);
        }
    }

    private static Object replay(String className, String methodName, Object key, String methodReturnType) {
        if (ContextManager.needReplay()) {
            DynamicClassExtractor extractor = new DynamicClassExtractor(className, methodName, new Object[]{key}, methodReturnType);
            MockResult mockResult = extractor.replay();
            if (mockResult != null && mockResult.notIgnoreMockResult()) {
                if (mockResult.getThrowable() != null) {
                    return mockResult.getThrowable();
                }
                return mockResult.getResult();
            }
        }
        return null;

    }
}
