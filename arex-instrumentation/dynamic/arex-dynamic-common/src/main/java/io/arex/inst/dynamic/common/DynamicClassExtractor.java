package io.arex.inst.dynamic.common;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.dynamic.common.listener.ListenableFutureAdapter;
import io.arex.inst.dynamic.common.listener.ResponseConsumer;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.DynamicClassEntity;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.LogUtil;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicClassExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClassExtractor.class);
    private static final int RESULT_SIZE_MAX = Integer.parseInt(System.getProperty("arex.dynamic.result.size.limit", "1000"));
    private static final String SERIALIZER = "gson";
    private static final String LISTENABLE_FUTURE = "com.google.common.util.concurrent.ListenableFuture";
    private static final String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";

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

    public DynamicClassExtractor(Method method, Object[] args, String keyExpression, Class<?> actualType) {
        this.clazzName = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        this.args = args;
        this.methodKey = buildMethodKey(method, args, keyExpression);
        this.methodReturnType = TypeUtil.getName(method.getReturnType());
        this.actualType = actualType;
    }

    public DynamicClassExtractor(Method method, Object[] args) {
        this.clazzName = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        this.args = args;
        this.methodKey = buildMethodKey(method, args);
        this.methodReturnType = TypeUtil.getName(method.getReturnType());
        this.actualType = null;
    }
    public void recordResponse(Object response) {
        if (response instanceof Future<?>) {
            this.setFutureResponse((Future<?>) response);
            return;
        }
        this.result = response;
        this.resultClazz = buildResultClazz(TypeUtil.getName(response));
        if (needRecord()) {
            this.serializedResult = Serializer.serialize(this.result, SERIALIZER);
            MockUtils.recordMocker(makeMocker());
            cacheMethodSignature();
        }
    }

    public MockResult replay() {
        String key = buildCacheKey();
        ArexContext context = ContextManager.currentContext();
        Object replayResult = context.getCachedReplayResultMap().get(key);
        if (replayResult == null) {
            Mocker replayMocker = MockUtils.replayMocker(makeMocker());
            if (MockUtils.checkResponseMocker(replayMocker)) {
                replayResult = Serializer.deserialize(replayMocker.getTargetResponse().getBody(),
                    TypeUtil.forName(replayMocker.getTargetResponse().getType()), SERIALIZER);
            }
            replayResult = restoreResponse(replayResult);
            // no key no cache, no parameter methods may return different values
            if (key != null && replayResult != null) {
                context.getCachedReplayResultMap().put(key, replayResult);
            }
        }
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(clazzName, methodName);
        return MockResult.success(ignoreMockResult, replayResult);
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
            return resultClazz + TypeUtil.HORIZONTAL_LINE + TypeUtil.getName(actualType.getName());
        }

        if (Config.get() == null || Config.get().getDynamicEntityMap().isEmpty()) {
            return resultClazz;
        }

        String signature = getDynamicEntitySignature();

        DynamicClassEntity dynamicEntity = Config.get().getDynamicEntity(signature);

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

        return Serializer.serialize(args, SERIALIZER);
    }

    String buildMethodKey(Method method, Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        if (Config.get() == null || Config.get().getDynamicEntityMap().isEmpty()) {
            return Serializer.serialize(args, SERIALIZER);
        }
        String signature = getDynamicEntitySignature();
        DynamicClassEntity dynamicEntity = Config.get().getDynamicEntity(signature);
        if (dynamicEntity == null || StringUtil.isEmpty(dynamicEntity.getAdditionalSignature())) {
            return Serializer.serialize(args, SERIALIZER);
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
        mocker.getTargetResponse().setBody(this.serializedResult);
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
        String logTitle = LogUtil.buildTitle("dynamic.needRecord");
        /*
         * Judge whether the hash value of the method signature has been recorded to avoid repeated recording.
         * The nonparametric method may return different results and needs to be recorded
         * */
        ArexContext context = ContextManager.currentContext();
        if (context != null && methodKey != null) {
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
        if (StringUtil.isEmpty(serializedResult)) {
            return String.format("%s_%s_%s_no_result", clazzName, methodName, methodKey);
        }
        return String.format("%s_%s_%s_has_result", clazzName, methodName, methodKey);
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

    private String buildCacheKey() {
        if (StringUtil.isNotEmpty(this.methodKey)) {
            return String.format("%s_%s_%s", this.clazzName, this.methodName, this.methodKey);
        }
        return StringUtil.EMPTY;
    }

    public String getSerializedResult() {
        return serializedResult;
    }
}