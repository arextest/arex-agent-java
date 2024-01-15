package io.arex.inst.dynamic.common;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.thirdparty.util.time.DateFormatUtils;
import io.arex.inst.common.util.FluxUtil;
import io.arex.inst.dynamic.common.listener.FluxConsumer;
import io.arex.inst.dynamic.common.listener.ListenableFutureAdapter;
import io.arex.inst.dynamic.common.listener.MonoConsumer;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.arex.inst.runtime.util.sizeof.AgentSizeOf;

public class DynamicClassExtractor {
    private static final String LISTENABLE_FUTURE = "com.google.common.util.concurrent.ListenableFuture";
    private static final String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";
    private static final String NEED_RECORD_TITLE = "dynamic.needRecord";
    private static final String NEED_REPLAY_TITLE = "dynamic.needReplay";
    public static final String MONO = "reactor.core.publisher.Mono";
    public static final String FLUX = "reactor.core.publisher.Flux";
    private static final String JODA_LOCAL_DATE_TIME = "org.joda.time.LocalDateTime";
    private static final String JODA_LOCAL_TIME = "org.joda.time.LocalTime";
    public static final String SIMPLE_DATE_FORMAT_MILLIS = "yyyy-MM-dd HH:mm:";
    private static final String SIMPLE_DATE_FORMAT_MILLIS_WITH_ZONE = "yyyy-MM-dd'T'HH:mm:";
    public static final String SHORT_TIME_FORMAT_MILLISECOND = "HH:mm:";
    private static final String TIME_ZONE = "ZZZ";
    private static final String ZERO_SECOND_TIME = "00.000";
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
    private static final AgentSizeOf agentSizeOf = AgentSizeOf.newInstance();

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

    public Object recordResponse(Object response) {
        if (IgnoreUtils.invalidOperation(dynamicSignature)) {
            LogManager.warn(NEED_RECORD_TITLE, StringUtil.format(
                    "do not record invalid operation: %s, can not serialize request or response or response too large",
                    dynamicSignature));
            return response;
        }
        if (response instanceof Future<?>) {
            this.setFutureResponse((Future<?>) response);
            return response;
        }
        // Compatible with not import package reactor-core
        if (MONO.equals(methodReturnType) && response instanceof Mono<?>) {
            return new MonoConsumer(this).accept((Mono<?>) response);
        }

        if (FLUX.equals(methodReturnType) && response instanceof Flux<?>) {
            return new FluxConsumer(this).accept((Flux<?>) response);
        }

        this.result = response;
        if (needRecord()) {
            this.resultClazz = buildResultClazz(TypeUtil.getName(response));
            Mocker mocker = makeMocker();
            mocker.getTargetResponse().setBody(getSerializedResult());
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

        return serialize(normalizeArgs(args));
    }

    String buildRequestType(Method method) {
        return ArrayUtils.toString(method.getParameterTypes(), obj -> ((Class<?>)obj).getTypeName());
    }

    /**
     * There will be a second-level difference between time type recording and playback,
     * resulting in inability to accurately match data. And in order to be compatible with previously recorded data,
     * the second time is cleared to zero.
     * ex: 2023-01-01 12:12:01.123 -> 2023-01-01 12:12:00.000
     */
    private Object[] normalizeArgs(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return args;
        }
        Object[] normalizedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            normalizedArgs[i] = normalizeArg(args[i]);
        }
        return normalizedArgs;
    }

    private Object normalizeArg(Object arg) {
        if (arg == null) {
            return null;
        }

        if (arg instanceof LocalDateTime) {
            return zeroTimeSecond(DateFormatUtils.format((LocalDateTime) arg, SIMPLE_DATE_FORMAT_MILLIS));
        }

        if (arg instanceof LocalTime) {
            return zeroTimeSecond(DateFormatUtils.format((LocalTime) arg, SHORT_TIME_FORMAT_MILLISECOND));
        }

        if (arg instanceof Calendar) {
            Calendar calendar = (Calendar) arg;
            String timeZone = DateFormatUtils.format(calendar, TIME_ZONE, calendar.getTimeZone());
            return zeroTimeSecond(DateFormatUtils.format(calendar, SIMPLE_DATE_FORMAT_MILLIS_WITH_ZONE, calendar.getTimeZone())) + timeZone;
        }

        if (arg instanceof Date) {
            return zeroTimeSecond(DateFormatUtils.format((Date) arg, SIMPLE_DATE_FORMAT_MILLIS));
        }

        if (JODA_LOCAL_DATE_TIME.equals(arg.getClass().getName())) {
            return zeroTimeSecond(((org.joda.time.LocalDateTime) arg).toString(SIMPLE_DATE_FORMAT_MILLIS));
        }

        if (JODA_LOCAL_TIME.equals(arg.getClass().getName())) {
            return zeroTimeSecond(((org.joda.time.LocalTime) arg).toString(SHORT_TIME_FORMAT_MILLISECOND));
        }

        return arg;
    }

    private String zeroTimeSecond(String text) {
        return text + ZERO_SECOND_TIME;
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
            return serialize(normalizeArgs(args));
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
        mocker.setMerge(true);
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
            return FluxUtil.restore(result);
        }
        return result;
    }

    private boolean needRecord() {
        /*
         * Judge whether the hash value of the method signature has been recorded to avoid repeated recording.
         * */
        ArexContext context = ContextManager.currentContext();
        if (context != null) {
            this.methodSignatureKey = buildDuplicateMethodKey();
            if (methodKey != null) {
                this.methodSignatureKeyHash = StringUtil.encodeAndHash(this.methodSignatureKey);
            } else {
                // no argument method check repeat by className + methodName + result
                this.methodSignatureKeyHash = buildNoArgMethodSignatureHash();
            }
            if (context.getMethodSignatureHashList().contains(this.methodSignatureKeyHash)) {
                if (Config.get().isEnableDebug()) {
                    LogManager.warn(NEED_RECORD_TITLE,
                            StringUtil.format("do not record method, cuz exist same method signature: %s", this.methodSignatureKey));
                }
                return false;
            }
        }

        if (this.result == null || this.result instanceof Throwable) {
            return true;
        }

        if (!agentSizeOf.checkMemorySizeLimit(this.result, ArexConstants.MEMORY_SIZE_1MB)) {
            IgnoreUtils.addInvalidOperation(dynamicSignature);
            LogManager.warn(NEED_RECORD_TITLE, StringUtil.format("dynamicClass:%s, exceed memory max limit:%s",
                    dynamicSignature, AgentSizeOf.humanReadableUnits(ArexConstants.MEMORY_SIZE_1MB)));
            return false;
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
            if (this.methodKey != null && this.methodSignatureKey != null) {
                context.getMethodSignatureHashList().add(this.methodSignatureKeyHash);
            } else {
                // no argument method check repeat by class+method+result
                context.getMethodSignatureHashList().add(buildNoArgMethodSignatureHash());
            }
        }
    }

    public String getSerializedResult() {
        if (this.serializedResult == null) {
            serializedResult = serialize(this.result);
        }
        return this.serializedResult;
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

    private int buildNoArgMethodSignatureHash() {
        return StringUtil.encodeAndHash(String.format("%s_%s_%s", this.clazzName, this.methodName, getSerializedResult()));
    }
}
