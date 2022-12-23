package io.arex.inst.dynamic;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicClassExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClassExtractor.class);
    private static final int RESULT_SIZE_MAX = Integer.parseInt(System.getProperty("arex.dynamic.result.size.limit", "1000"));

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
        this.operationKey = Serializer.serialize(args, "gson");
        this.operationResult = Serializer.serialize(result, "gson");
        this.resultClazz = TypeUtil.getName(result);
        this.result = result;
    }

    public void record() {
        if (needRecord()) {
            MockUtils.recordMocker(makeMocker());
            cacheMethodSignature();
        }
    }

    private Mocker makeMocker() {
        Mocker mocker = MockUtils.createDynamicClass(this.clazzName, this.operation);
        mocker.getTargetRequest().setBody(this.operationKey);
        mocker.getTargetResponse().setBody(this.operationResult);
        mocker.getTargetResponse().setType(this.resultClazz);
        return mocker;
    }

    public MockResult replay() {
        String key = buildCacheKey();
        ArexContext context = ContextManager.currentContext();
        Object replayResult = context.getCachedReplayResultMap().get(key);
        if (replayResult == null) {
            Mocker replayMocker = MockUtils.replayMocker(makeMocker());
            if (MockUtils.checkResponseMocker(replayMocker)) {
                replayResult = Serializer.deserialize(replayMocker.getTargetResponse().getBody(),
                    replayMocker.getTargetResponse().getType());
            }
            // no key no cache, no parameter methods may return different values
            if (key != null && replayResult != null) {
                context.getCachedReplayResultMap().put(key, replayResult);
            }
        }
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(clazzName, operation);
        return MockResult.success(ignoreMockResult, replayResult);
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

        if (result == null || result instanceof Throwable) {
            return true;
        }

        try {
            int size = 0;
            if (result instanceof Collection<?>) {
                size = ((Collection<?>) result).size();
            } else if (result instanceof Map<?, ?>) {
                size = ((Map<?, ?>) result).size();
            } else if (result.getClass().isArray()) {
                size = Array.getLength(result);
            }
            if (size > RESULT_SIZE_MAX) {
                LOGGER.warn("{} do not record method, cuz result size:{} > max limit: {}, method info: {}",
                    logTitle, size, RESULT_SIZE_MAX, methodSignatureKey);
                return false;
            }
        } catch (Throwable e) {
            LOGGER.warn(logTitle, e);
        }
        return true;
    }

    private String buildDuplicateMethodKey() {
        if (StringUtil.isEmpty(operationResult)) {
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
        return StringUtil.EMPTY;
    }
}