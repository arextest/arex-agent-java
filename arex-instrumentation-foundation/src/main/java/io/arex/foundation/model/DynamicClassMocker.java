package io.arex.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.arex.foundation.serializer.SerializeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class DynamicClassMocker extends AbstractMocker {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClassMocker.class);

    @JsonProperty("clazzName")
    private String clazzName;
    @JsonProperty("operation")
    private String operation;
    @JsonProperty("operationKey")
    private String operationKey;
    @JsonProperty("operationResult")
    private String operationResult;
    @JsonProperty("resultClazz")
    private String resultClazz;

    @SuppressWarnings("deserialize")
    public DynamicClassMocker() {
        super(MockerCategory.DYNAMIC_CLASS);
    }
    public DynamicClassMocker(String clazzName, String operation, String operationKey) {
        this(clazzName, operation, operationKey, null, null);
    }
    public DynamicClassMocker(String clazzName, String operation, String operationKey, String operationResult, String resultClazz) {
        super(MockerCategory.DYNAMIC_CLASS);

        this.clazzName = clazzName;
        this.operation = operation;
        this.operationKey = operationKey;
        this.operationResult = operationResult;
        this.resultClazz = resultClazz;
    }

    @Override
    public Object parseMockResponse(AbstractMocker requestMocker) {
        Object response = SerializeUtils.deserialize(this.operationResult, this.resultClazz);
        if (response == null) {
            LOGGER.warn("{}deserialize response is null. response type:{}, response: {}", getReplayLogTitle(), this.resultClazz, this.operationResult);
            return null;
        }

        return response;
    }

    public String getClazzName() {
        return clazzName;
    }

    public String getOperation() {
        return operation;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public String getOperationResult() {
        return operationResult;
    }

    public String getResultClazz() {
        return resultClazz;
    }

    @Override
    protected Predicate<DynamicClassMocker> filterLocalStorage() {
        return mocker -> {
            if (StringUtils.isNotBlank(clazzName) && !StringUtils.equals(clazzName, mocker.getClazzName())) {
                return false;
            }
            if (StringUtils.isNotBlank(operation) && !StringUtils.equals(operation, mocker.getOperation())) {
                return false;
            }
            if (StringUtils.isNotBlank(operationKey) && !StringUtils.equals(operationKey, mocker.getOperationKey())) {
                return false;
            }
            return true;
        };
    }
}
