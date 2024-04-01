package io.arex.inst.dynamic.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.common.util.FluxRecordFunction;
import io.arex.inst.common.util.FluxReplayUtil;
import io.arex.inst.common.util.FluxReplayUtil.FluxResult;
import io.arex.inst.common.util.MonoRecordFunction;
import io.arex.inst.dynamic.common.listener.ListenableFutureAdapter;
import io.arex.inst.dynamic.common.listener.ResponseConsumer;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.*;
import io.arex.inst.runtime.log.LogManager;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

import io.arex.inst.runtime.util.sizeof.ThrowableFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.arex.inst.runtime.util.sizeof.AgentSizeOf;

public class DynamicClassExtractor {
    private static final String LISTENABLE_FUTURE = "com.google.common.util.concurrent.ListenableFuture";
    public static final String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";
    public static final String GUAVA_IMMUTABLE_MAP = "com.google.common.collect.ImmutableMap";
    private static final String NEED_RECORD_TITLE = "dynamic.needRecord";
    private static final String NEED_REPLAY_TITLE = "dynamic.needReplay";
    public static final String MONO = "reactor.core.publisher.Mono";
    public static final String FLUX = "reactor.core.publisher.Flux";
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
    private final String requestType;
    private boolean isExceedMaxSize;
    private static final AgentSizeOf agentSizeOf = AgentSizeOf.newInstance(ThrowableFilter.INSTANCE);

    public DynamicClassExtractor(Method method, Object[] args, String keyExpression, Class<?> actualType) {
        this.clazzName = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        this.args = args;
        this.dynamicSignature = getDynamicEntitySignature();
        this.methodKey = buildMethodKey(method, args, keyExpression);
        this.methodReturnType = TypeUtil.getName(method.getReturnType());
        this.actualType = actualType;
        this.requestType = buildRequestType(method);
    }

    public DynamicClassExtractor(Method method, Object[] args) {
        this.clazzName = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        this.args = args;
        this.dynamicSignature = getDynamicEntitySignature();
        this.methodKey = buildMethodKey(method, args);
        this.methodReturnType = TypeUtil.getName(method.getReturnType());
        this.actualType = null;
        this.requestType = buildRequestType(method);
    }

    public DynamicClassExtractor(String clazzName, String methodName, Object[] args, String methodReturnType) {
        this.clazzName = clazzName;
        this.methodName = methodName;
        this.args = args;
        this.dynamicSignature = getDynamicEntitySignature();
        this.methodKey = serialize(args, ArexConstants.GSON_REQUEST_SERIALIZER);
        this.methodReturnType = methodReturnType;
        this.actualType = null;
        this.requestType = null;
    }

    public Object recordResponse(Object response) {
        if (IgnoreUtils.invalidOperation(dynamicSignature)) {
            LogManager.warn(NEED_RECORD_TITLE, StringUtil.format(
                    "do not record invalid operation: %s, can not serialize request/response or response is exceed memory max limit.",
                    dynamicSignature));
            return response;
        }
        if (response instanceof Future<?>) {
            this.setFutureResponse((Future<?>) response);
            return response;
        }
        // Compatible with not import package reactor-core
        if (MONO.equals(methodReturnType) && response instanceof Mono<?>) {
            Function<Object, Void> executor = mockResult -> {
                this.recordResponse(mockResult);
                return null;
            };
            return new MonoRecordFunction(executor).apply((Mono<?>) response);
        }

        if (FLUX.equals(methodReturnType) && response instanceof Flux<?>) {
            Function<FluxResult, Void> executor = mockResult -> {
                this.recordResponse(mockResult);
                return null;
            };
            return new FluxRecordFunction(executor).apply((Flux<?>) response);
        }

        this.result = response;
        if (needRecord()) {
            this.resultClazz = buildResultClazz(TypeUtil.getName(response));
            Mocker mocker = makeMocker();
            mocker.getTargetResponse().setBody(getSerializedResult());
            mocker.getTargetResponse().setAttribute(ArexConstants.EXCEED_MAX_SIZE_FLAG, this.isExceedMaxSize);
            MockUtils.recordMocker(mocker);
            cacheMethodSignature();
        }
        return response;
    }

    public MockResult replay() {
        if (IgnoreUtils.invalidOperation(dynamicSignature)) {
            LogManager.warn(NEED_REPLAY_TITLE,
                    StringUtil.format("do not replay invalid operation: %s, can not serialize args or response", dynamicSignature));
            return MockResult.IGNORE_MOCK_RESULT;
        }
        Object replayResult = null;
        Mocker replayMocker = MockUtils.replayMocker(makeMocker(), MockStrategyEnum.FIND_LAST);
        if (MockUtils.checkResponseMocker(replayMocker)) {
            String typeName = replayMocker.getTargetResponse().getType();
            String replayBody = replayMocker.getTargetResponse().getBody();
            replayResult = deserializeResult(replayBody, typeName);
        }
        replayResult = restoreResponse(replayResult);
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(clazzName, methodName);
        return MockResult.success(ignoreMockResult, replayResult);
    }

    public MockResult replayOrRealCall() {
        MockResult mockResult = replay();
        if (mockResult != null && mockResult.getResult() == null) {
            return MockResult.IGNORE_MOCK_RESULT;
        }
        return mockResult;
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
        if (key != null || StringUtil.isNotEmpty(keyExpression)) {
            return key;
        }

        return serialize(args, ArexConstants.GSON_REQUEST_SERIALIZER);
    }

    String buildRequestType(Method method) {
        return ArrayUtils.toString(method.getParameterTypes(), obj -> ((Class<?>)obj).getTypeName());
    }

    String buildMethodKey(Method method, Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        if (Config.get() == null || Config.get().getDynamicClassSignatureMap().isEmpty()) {
            return serialize(args, ArexConstants.GSON_REQUEST_SERIALIZER);
        }

        DynamicClassEntity dynamicEntity = Config.get().getDynamicEntity(dynamicSignature);
        if (dynamicEntity == null || StringUtil.isEmpty(dynamicEntity.getAdditionalSignature())) {
            return serialize(args, ArexConstants.GSON_REQUEST_SERIALIZER);
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
        mocker.setNeedMerge(true);
        mocker.getTargetRequest().setBody(this.methodKey);
        mocker.getTargetResponse().setType(this.resultClazz);
        mocker.getTargetRequest().setType(this.requestType);
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

        if (MONO.equals((this.methodReturnType))) {
            if (result instanceof Throwable) {
                return Mono.error((Throwable) result);
            }
            return Mono.justOrEmpty(result);
        }

        if (FLUX.equals(this.methodReturnType)) {
            if (result instanceof Throwable) {
                return Flux.error((Throwable) result);
            }
            return FluxReplayUtil.restore(result);
        }
        if (GUAVA_IMMUTABLE_MAP.equals(this.methodReturnType)) {
            return ImmutableMap.builder().putAll((Map<?, ?>) result).build();
        }
        return result;
    }

    private boolean needRecord() {
        // Judge whether the hash value of the method signature has been recorded to avoid repeated recording.
        ArexContext context = ContextManager.currentContext();
        if (context != null) {
            this.methodSignatureKey = buildDuplicateMethodKey();
            if (methodKey != null) {
                this.methodSignatureKeyHash = StringUtil.encodeAndHash(this.methodSignatureKey);
            } else {
                /*
                 * no argument method check repeat first by className + methodName
                 * avoid serialize big result(maybe last record result exceed size limit and already was set to empty)
                 * so first only check className + methodName
                 */
                this.methodSignatureKeyHash = buildNoArgMethodSignatureHash(false);
                if (!context.getMethodSignatureHashList().contains(this.methodSignatureKeyHash)) {
                    // if missed means no exceed size limit, check className + methodName + result
                    this.methodSignatureKeyHash = buildNoArgMethodSignatureHash(true);
                }
            }
            if (context.getMethodSignatureHashList().contains(this.methodSignatureKeyHash)) {
                if (Config.get().isEnableDebug()) {
                    LogManager.warn(NEED_RECORD_TITLE,
                            StringUtil.format("do not record method, cuz exist same method signature: %s", this.methodSignatureKey));
                }
                return false;
            }
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
        if (context != null) {
            context.getMethodSignatureHashList().add(this.methodSignatureKeyHash);
        }
    }

    public String getSerializedResult() {
        if (this.serializedResult == null && !this.isExceedMaxSize) {
            if (!agentSizeOf.checkMemorySizeLimit(this.result, ArexConstants.MEMORY_SIZE_1MB)) {
                this.isExceedMaxSize = true;
                LogManager.warn(ArexConstants.EXCEED_MAX_SIZE_TITLE, StringUtil.format("method:%s, exceed memory max limit:%s, " +
                                "record result will be null, please check method return size, suggest replace it",
                        this.dynamicSignature, AgentSizeOf.humanReadableUnits(ArexConstants.MEMORY_SIZE_1MB)));
                IgnoreUtils.addInvalidOperation(dynamicSignature);
                return null;
            }
            this.serializedResult = serialize(this.result, ArexConstants.GSON_SERIALIZER);
        }
        return this.serializedResult;
    }

    private String serialize(Object object, String serializer) {
        if (IgnoreUtils.invalidOperation(dynamicSignature)) {
            return null;
        }
        try {
            return Serializer.serializeWithException(object, serializer);
        } catch (Throwable ex) {
            IgnoreUtils.addInvalidOperation(dynamicSignature);
            LogManager.warn("serializeWithException", StringUtil.format("can not serialize object: %s, cause: %s", TypeUtil.errorSerializeToString(object), ex.toString()));
            return null;
        }
    }

    private int buildNoArgMethodSignatureHash(boolean isNeedResult) {
        if (isNeedResult) {
            return StringUtil.encodeAndHash(String.format("%s_%s_%s", this.clazzName, this.methodName, getSerializedResult()));
        }
        return StringUtil.encodeAndHash(String.format("%s_%s_%s", this.clazzName, this.methodName, null));
    }
}
