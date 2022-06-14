package io.arex.foundation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public class DynamicClassMocker extends AbstractMocker {

    @JsonProperty("clazzName")
    private String clazzName;
    @JsonProperty("operation")
    private String operation;
    @JsonProperty("operationKey")
    private String operationKey;


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
        this.setResponse(operationResult);
        this.setResponseType(resultClazz);
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