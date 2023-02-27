package io.arex.inst.dynamic;

import com.google.common.util.concurrent.Futures;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.dynamic.listener.ListenableFutureAdapt;
import io.arex.inst.dynamic.listener.ResponseConsumer;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicClassExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClassExtractor.class);
    private static final int RESULT_SIZE_MAX = Integer.parseInt(System.getProperty("arex.dynamic.result.size.limit", "1000"));
    private static final String SERIALIZER = "gson";
    private static final String LISTENABLE_FUTURE = "com.google.common.util.concurrent.ListenableFuture";
    private static final String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";

    private final String clazzName;
    private final String operation;
    private final String operationKey;
    private String operationResult;
    private Object result;
    private String resultClazz;
    private transient String methodSignatureKey;
    private final String methodReturnType;
    private int methodSignatureKeyHash;

    private static List<DynamicClassEntity> genericReturnTypeEntities = new ArrayList<>();

    public DynamicClassExtractor(String clazzName, String operation, Object[] args, String methodReturnType) {
        this.clazzName = clazzName;
        this.operation = operation;
        this.operationKey = Serializer.serialize(args, SERIALIZER);
        this.methodReturnType = methodReturnType;
    }

    public void setFutureResponse(Future<?> result) {
        if (result instanceof CompletableFuture) {
            ((CompletableFuture<?>) result).whenComplete(new ResponseConsumer(this));
            return;
        }

        if (LISTENABLE_FUTURE.equals(methodReturnType)) {
            new ListenableFutureAdapt(this).addCallBack(result);
        }
    }

    public void setResponse(Object response) {
        this.result = response;
        this.resultClazz = buildResultClazz(TypeUtil.getName(response));
        record();
    }

    private String buildResultClazz(String resultClazz) {
       updateEntities();

       if (resultClazz == null) {
           return null;
       }

       if (resultClazz.contains(TypeUtil.HORIZONTAL_LINE_STR)) {
            return resultClazz;
        }

       for (DynamicClassEntity entity : genericReturnTypeEntities) {
           if (Objects.equals(clazzName, entity.getClazzName()) && Objects.equals(operation, entity.getOperation())) {
               return resultClazz + TypeUtil.HORIZONTAL_LINE + entity.getKeyFormula();
           }
       }
       return resultClazz;
    }

    private void updateEntities() {
        List<DynamicClassEntity> entities = Config.get().dynamicClassEntities().stream()
                .filter(DynamicClassEntity::isGenericReturnType).collect(Collectors.toList());
        if (!Objects.equals(entities.toString(), genericReturnTypeEntities.toString())) {
            genericReturnTypeEntities = entities;
        }
    }

    public void record() {
        if (needRecord()) {
            this.operationResult = Serializer.serialize(result, SERIALIZER);
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
                    TypeUtil.forName(replayMocker.getTargetResponse().getType()), SERIALIZER);
            }
            replayResult = restoreResponse(replayResult);
            // no key no cache, no parameter methods may return different values
            if (key != null && replayResult != null) {
                context.getCachedReplayResultMap().put(key, replayResult);
            }
        }
        boolean ignoreMockResult = IgnoreUtils.ignoreMockResult(clazzName, operation);
        return MockResult.success(ignoreMockResult, replayResult);
    }

    protected Object restoreResponse(Object result) {
        if (this.methodReturnType == null) {
            return result;
        }

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
                return completableFuture;
            }
            completableFuture.complete(result);
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