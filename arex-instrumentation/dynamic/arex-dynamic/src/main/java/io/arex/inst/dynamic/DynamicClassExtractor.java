package io.arex.inst.dynamic;

import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.context.ArexContext;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.model.DynamicClassMocker;
import io.arex.foundation.model.MockResult;
import io.arex.foundation.serializer.GsonSerializer;
import io.arex.foundation.util.LogUtil;
import io.arex.foundation.util.StringUtil;
import io.arex.foundation.util.TypeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class DynamicClassExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClassExtractor.class);

    private final String clazzName;
    private final String operation;
    private final String operationKey;
    private final String operationResult;
    private final Object result;
    private final String resultClazz;
    private transient String methodSignatureKey;
    private int methodSignatureKeyHash;

    public DynamicClassExtractor(String clazzName, String operation, Object[] args) {
        this(clazzName, operation, args, null);
    }
    public DynamicClassExtractor(String clazzName, String operation, Object[] args, Object result) {
        this.clazzName = clazzName;
        this.operation = operation;
        this.operationKey = GsonSerializer.INSTANCE.serialize(args);
        this.operationResult = GsonSerializer.INSTANCE.serialize(result);
        this.resultClazz = TypeUtil.getName(result);
        this.result = result;
    }

    public void record() {
        if (needRecord()) {
            DynamicClassMocker mocker = new DynamicClassMocker(this.clazzName, this.operation, this.operationKey, this.operationResult, this.resultClazz);
            mocker.record();
            cacheMethodSignature();
        }
    }

    public MockResult replay() {
        if (!ContextManager.needReplay()) {
            return null;
        }
        String key = buildCacheKey();
        ArexContext context = ContextManager.currentContext();
        MockResult mockResult = (MockResult)context.getCacheMap().get(key);
        if (mockResult == null) {
            DynamicClassMocker mocker = new DynamicClassMocker(this.clazzName, this.operation, this.operationKey);
            Object replayResult = mocker.replay();
            mockResult = MockResult.of(mocker.ignoreMockResult(), replayResult);
            // no key no cache, no parameter methods may return different values
            if (key != null && replayResult != null) {
                context.getCacheMap().put(key, mockResult);
            }
        }
        return mockResult;
    }

    private boolean needRecord() {
        String logTitle = LogUtil.buildTitle("dynamic.needRecord");
        /*
         * Judge whether the hash value of the method signature has been recorded to avoid repeated recording.
         * The nonparametric method may return different results and needs to be recorded
         * */
        ArexContext context = ContextManager.currentContext();
        if (context != null && operationKey != null) {
            this.methodSignatureKey = buildDuplicateMethodKey();
            this.methodSignatureKeyHash = StringUtil.encodeAndHash(methodSignatureKey);
            if (context.getMethodSignatureHashList().contains(methodSignatureKeyHash)) {
                LOGGER.warn("{}do not record method, cuz exist same method signature:{}",
                        logTitle, methodSignatureKey);
                return false;
            }
        }

        if (result == null) {
            return true;
        }

        try {
            int size = 0;
            if (result instanceof Collection<?>) {
                size = ((Collection<?>)result).size();
            } else if (result instanceof Map<?, ?>) {
                size = ((Map<?, ?>)result).size();
            } else if (result.getClass().isArray()) {
                size = Array.getLength(result);
            }
            if (size > ConfigManager.INSTANCE.getDynamicResultSizeLimit()) {
                LOGGER.warn("{}do not record method, cuz result size:{} > max limit: {}, method info: {}",
                        logTitle, size, ConfigManager.INSTANCE.getDynamicResultSizeLimit(), methodSignatureKey);
                return false;
            }
        } catch (Throwable e) {
            LOGGER.warn(logTitle, e);
        }
        return true;
    }

    private String buildDuplicateMethodKey() {
        if (StringUtils.isEmpty(operationResult)) {
            return String.format("%s_%s_%s_no_result", clazzName, operation, operationKey);
        }
        return String.format("%s_%s_%s_has_result", clazzName, operation, operationKey);
    }

    /**
     * cache dynamic method with hashcode of signature,in order to filter out duplicate next record
     */
    private void cacheMethodSignature() {
        ArexContext context = ContextManager.currentContext();
        if (context != null && this.operationKey != null && this.methodSignatureKey != null) {
            context.getMethodSignatureHashList().add(this.methodSignatureKeyHash);
        }
    }

    private String buildCacheKey() {
        if (StringUtil.isNotEmpty(this.operationKey)) {
            return String.format("%s_%s_%s", this.clazzName, this.operation, this.operationKey);
        }
        return null;
    }
}

