package io.arex.inst.dynamic.common;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.dynamic.common.listener.ListenableFutureAdapter;
import io.arex.inst.dynamic.common.listener.ResponseConsumer;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeResultDTO;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.*;
import io.arex.inst.runtime.log.LogManager;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class DynamicClassExtractor {
    private static final int RESULT_SIZE_MAX = Integer.parseInt(System.getProperty("arex.dynamic.result.size.limit", "1000"));
    private static final String LISTENABLE_FUTURE = "com.google.common.util.concurrent.ListenableFuture";
    private static final String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";
    private static final String NEED_RECORD_TITLE = "dynamic.needRecord";
    private static final String NEED_REPLAY_TITLE = "dynamic.needReplay";
    private final String clazzName;
    private final String methodName;
    private final String methodKey;
    private String serializedResult;
    private Object result;
    private String resultClazz;
    private String methodSignatureKey;
    private final String methodReturnType;
    private int methodSignatureKeyHash;
    private final Class<?> actualType;
    private final Object[] args;
    private final String dynamicSignature;

    public DynamicClassExtractor(Method method, Object[] args, String keyExpression, Class<?> actualType) {
        this.clazzName = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        this.args = args;
        this.dynamicSignature = getDynamicEntitySignature();
        this.methodKey = buildMethodKey(method, args, keyExpression);
        this.methodReturnType = TypeUtil.getName(method.getReturnType());
        this.actualType = actualType;
    }

    public DynamicClassExtractor(Method method, Object[] args) {
        this.clazzName = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        this.args = args;
        this.dynamicSignature = getDynamicEntitySignature();
        this.methodKey = buildMethodKey(method, args);
        this.methodReturnType = TypeUtil.getName(method.getReturnType());
        this.actualType = null;
    }
    public void recordResponse(Object response) {
        if (IgnoreUtils.invalidOperation(dynamicSignature)) {
            LogManager.warn(NEED_RECORD_TITLE,
                    StringUtil.format("do not record invalid operation: %s, can not serialize args or response", dynamicSignature));
            return;
        }
        if (response instanceof Future<?>) {
            this.setFutureResponse((Future<?>) response);
            return;
        }
        this.result = response;
        if (needRecord()) {
            this.resultClazz = buildResultClazz(TypeUtil.getName(response));
            Mocker mocker = makeMocker();
            // merge record, no parameter method not merge(currently only support accurate match)
            if (Config.get().getBoolean(ArexConstants.MERGE_RECORD_ENABLE, true) && StringUtil.isNotEmpty(this.methodKey)) {
                buildMergeMocker(mocker);
            } else {
                this.serializedResult = serialize(this.result);
                mocker.getTargetResponse().setBody(this.serializedResult);
            }
            MockUtils.recordMocker(mocker);
            cacheMethodSignature();
        }
    }

    private void buildMergeMocker(Mocker mocker) {
        MergeResultDTO mergeResultDTO = MergeResultDTO.of(MockCategoryType.DYNAMIC_CLASS.getName(),
                this.clazzName,
                this.methodName,
                this.args,
                this.result, // do not serialize(this.result), avoid generate new json string, will increase memory
                this.resultClazz,
                buildMethodSignatureKey(),
                ArexConstants.GSON_SERIALIZER);
        mocker.getTargetRequest().setAttribute(ArexConstants.MERGE_RECORD_KEY, mergeResultDTO);
    }

    public MockResult replay() {
        if (IgnoreUtils.invalidOperation(dynamicSignature)) {
            LogManager.warn(NEED_REPLAY_TITLE,
                    StringUtil.format("do not replay invalid operation: %s, can not serialize args or response", dynamicSignature));
            return MockResult.IGNORE_MOCK_RESULT;
        }
        int signatureHashKey = buildMethodSignatureKey();
        Map<Integer, MergeResultDTO> cachedReplayResultMap = ContextManager.currentContext().getCachedReplayResultMap();
        Object replayResult = null;
        // First get replay result from cache
        MergeResultDTO mergeResultDTO = cachedReplayResultMap.get(signatureHashKey);
        String replayBody;
        if (mergeResultDTO != null && MockCategoryType.DYNAMIC_CLASS.getName().equals(mergeResultDTO.getCategory())) {
            replayBody = Serializer.serialize(mergeResultDTO.getResult(), ArexConstants.GSON_SERIALIZER);
            replayResult = deserializeResult(replayBody, mergeResultDTO.getResultClazz());
        } else {
            // compatible with old process logic: single replay
            // If not in cache, get replay result from mock server
            Mocker replayMocker = MockUtils.replayMocker(makeMocker(), MockStrategyEnum.FIND_LAST);
            String typeName = "";
            if (MockUtils.checkResponseMocker(replayMocker)) {
                typeName = replayMocker.getTargetResponse().getType();
                replayBody = replayMocker.getTargetResponse().getBody();
                replayResult = deserializeResult(replayBody, typeName);
            }
            // no parameter no cache, no parameter methods may return different values
            if (StringUtil.isNotEmpty(this.methodKey) && replayResult != null) {
                mergeResultDTO = MergeResultDTO.of(MockCategoryType.DYNAMIC_CLASS.getName(), this.clazzName,
                        this.methodName, this.args, replayResult, typeName, signatureHashKey, null);
                cachedReplayResultMap.put(signatureHashKey, mergeResultDTO);
            }
        }
        replayResult = restoreResponse(replayResult);
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(clazzName, methodName);
        return MockResult.success(ignoreMockResult, replayResult);
    }

    private Object deserializeResult(String replayResult, String typeName) {
        return Serializer.deserialize(replayResult, typeName, ArexConstants.GSON_SERIALIZER);
    }

    void setFutureResponse(Future<?> result) {
        if (result instanceof CompletableFuture) {
            ((CompletableFuture<?>) result).whenComplete(new ResponseConsumer(this));
            return;
        }

        // Compatible with not import Guava
        if (LISTENABLE_FUTURE.equals(methodReturnType)) {
            ListenableFutureAdapter.addCallBack((ListenableFuture<?>) result, this);
        }
    }

    String buildResultClazz(String resultClazz) {
        if (StringUtil.isEmpty(resultClazz) || resultClazz.contains(TypeUtil.HORIZONTAL_LINE_STR)) {
            return resultClazz;
        }

        // @ArexMock actualType
        if (actualType != null && Object.class != actualType) {
            return resultClazz + TypeUtil.HORIZONTAL_LINE + actualType.getName();
        }

        if (Config.get() == null || Config.get().getDynamicClassSignatureMap().isEmpty()) {
            return resultClazz;
        }

        DynamicClassEntity dynamicEntity = Config.get().getDynamicEntity(dynamicSignature);

        if (dynamicEntity == null || StringUtil.isEmpty(dynamicEntity.getActualType())) {
            return resultClazz;
        }

        return resultClazz + TypeUtil.HORIZONTAL_LINE + dynamicEntity.getActualType();
    }

    String buildMethodKey(Method method, Object[] args, String keyExpression) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        String key = ExpressionParseUtil.generateKey(method, args, keyExpression);
        if (key != null) {
            return key;
        }

        return serialize(args);
    }

    String buildMethodKey(Method method, Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        if (Config.get() == null || Config.get().getDynamicClassSignatureMap().isEmpty()) {
            return serialize(args);
        }

        DynamicClassEntity dynamicEntity = Config.get().getDynamicEntity(dynamicSignature);
        if (dynamicEntity == null || StringUtil.isEmpty(dynamicEntity.getAdditionalSignature())) {
            return serialize(args);
        }

        String keyExpression = ExpressionParseUtil.replaceToExpression(method, dynamicEntity.getAdditionalSignature());

        return buildMethodKey(method, args, keyExpression);
    }

    private String getDynamicEntitySignature() {
        if (ArrayUtils.isEmpty(this.args)) {
            return clazzName + methodName;
        }

        return clazzName + methodName + args.length;
    }

    private Mocker makeMocker() {
        Mocker mocker = MockUtils.createDynamicClass(this.clazzName, this.methodName);
        mocker.getTargetRequest().setBody(this.methodKey);
        mocker.getTargetResponse().setType(this.resultClazz);
        return mocker;
    }

    Object restoreResponse(Object result) {
        if (LISTENABLE_FUTURE.equals(this.methodReturnType)) {
            if (result instanceof Throwable) {
                return Futures.immediateFailedFuture((Throwable) result);
            }
            return Futures.immediateFuture(result);
        }

        if (COMPLETABLE_FUTURE.equals(this.methodReturnType)) {
            CompletableFuture<Object> completableFuture = new CompletableFuture<>();
            if (result instanceof Throwable) {
                completableFuture.completeExceptionally((Throwable) result);
            } else {
                completableFuture.complete(result);
            }

            return completableFuture;
        }

        return result;
    }

    private boolean needRecord() {
        /*
         * Judge whether the hash value of the method signature has been recorded to avoid repeated recording.
         * The nonparametric method may return different results and needs to be recorded
         * */
        ArexContext context = ContextManager.currentContext();
        if (context != null && methodKey != null) {
            this.methodSignatureKey = buildDuplicateMethodKey();
            this.methodSignatureKeyHash = StringUtil.encodeAndHash(methodSignatureKey);
            if (context.getMethodSignatureHashList().contains(methodSignatureKeyHash)) {
                if (Config.get().isEnableDebug()) {
                    LogManager.warn(NEED_RECORD_TITLE,
                            StringUtil.format("do not record method, cuz exist same method signature: %s", methodSignatureKey));
                }
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
                String methodInfo = methodSignatureKey == null ? buildDuplicateMethodKey() : methodSignatureKey;
                LogManager.warn(NEED_RECORD_TITLE,
                        StringUtil.format("do not record method, cuz result size:%s > max limit: %s, method info: %s",
                                String.valueOf(size), String.valueOf(RESULT_SIZE_MAX), methodInfo));
                return false;
            }
        } catch (Throwable e) {
            LogManager.warn(NEED_RECORD_TITLE, e);
        }
        return true;
    }

    private String buildDuplicateMethodKey() {
        if (Objects.isNull(result)) {
            return String.format("%s_%s_%s_no_result", clazzName, methodName, methodKey);
        }
        return String.format("%s_%s_%s_has_result_%s", clazzName, methodName, methodKey, getResultKey());
    }

    private String getResultKey() {
        String resultClassName = result.getClass().getName();
        if (result instanceof Collection<?>) {
            return resultClassName + ((Collection<?>) result).size();
        }
        if (result instanceof Map<?, ?>) {
            return resultClassName + ((Map<?, ?>) result).size();
        }
        if (result.getClass().isArray()) {
            return resultClassName + Array.getLength(result);
        }
        return resultClassName;
    }

    /**
     * cache dynamic method with hashcode of signature,in order to filter out duplicate next record
     */
    private void cacheMethodSignature() {
        ArexContext context = ContextManager.currentContext();
        if (context != null && this.methodKey != null && this.methodSignatureKey != null) {
            context.getMethodSignatureHashList().add(this.methodSignatureKeyHash);
        }
    }

    public String getSerializedResult() {
        return serializedResult;
    }

    private String serialize(Object object) {
        if (IgnoreUtils.invalidOperation(dynamicSignature)) {
            return null;
        }
        try {
            return Serializer.serializeWithException(object, ArexConstants.GSON_SERIALIZER);
        } catch (Throwable ex) {
            IgnoreUtils.addInvalidOperation(dynamicSignature);
            LogManager.warn("serializeWithException", StringUtil.format("can not serialize object: %s, cause: %s", TypeUtil.errorSerializeToString(object), ex.toString()));
            return null;
        }
    }

    private int buildMethodSignatureKey() {
        return StringUtil.encodeAndHash(String.format("%s_%s_%s", clazzName, methodName, methodKey));
    }
}
