package io.arex.inst.runtime.model;


import io.arex.agent.bootstrap.util.CollectionUtil;
import java.util.ArrayList;
import java.util.List;

import io.arex.agent.bootstrap.util.StringUtil;

/**
 * DynamicClassEntity
 */
public class DynamicClassEntity {
    private static final String ACTUAL_TYPE_SIGNATURE = "T:";
    private final String clazzName;
    private final String operation;
    private final String parameterTypes;
    private String additionalSignature;
    private String actualType;
    private List<String> parameters;
    public DynamicClassEntity(String clazzName, String operation, String parameterTypes, String additionalSignature) {
        this.clazzName = clazzName;
        this.operation = operation;
        this.parameterTypes = parameterTypes;

        if (StringUtil.isNotEmpty(parameterTypes) && !"null".equals(parameterTypes)) {
            String[] types = StringUtil.split(parameterTypes, '@');
            this.parameters = new ArrayList<>(types.length);
            for (int i = 0; i < types.length; i++) {
                if (types[i] != null && types[i].length() > 0) {
                    this.parameters.add(types[i]);
                }
            }
        }
        // fix dynamic response type is generic type
        transformAdditionalSignature(additionalSignature);
    }

    /**
     * @param additionalSignature :
     * 1、java.lang.System.currentTimeMillis/java.util.UUID.randomUUID: indicates that there is a System.currentTimeMillis()/UUID.randomUUID() call in the method, and it will be replaced in the DynamicClassInstrumentation
     * 2、"T:xx": indicates that the return type of this method contains generic types, and (xx) is actual type ex:Optional<xx>
     * 3、String.EMPTY: normal dynamic method
     * 4、others: invalid config addition signature
     */
    private void transformAdditionalSignature(String additionalSignature) {
        if (isReplaceMethodSignature(additionalSignature)) {
            this.additionalSignature = additionalSignature;
            this.actualType = null;
            return;
        }

        if (additionalSignature.startsWith(ACTUAL_TYPE_SIGNATURE)) {
            this.actualType = StringUtil.substring(additionalSignature, ACTUAL_TYPE_SIGNATURE.length());
            this.additionalSignature = StringUtil.EMPTY;
            return;
        }

        this.additionalSignature = StringUtil.EMPTY;
        this.actualType = null;
    }

    private boolean isReplaceMethodSignature(String additionalSignature) {
        return StringUtil.isEmpty(additionalSignature) ||
            ArexConstants.UUID_SIGNATURE.equals(additionalSignature) ||
            ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE.equals(additionalSignature) ||
            ArexConstants.NEXT_INT_SIGNATURE.equals(additionalSignature) ||
            // Compatible with $1.getVal()
            additionalSignature.contains("$");
    }

    public String getSignature() {
        if (CollectionUtil.isEmpty(parameters)) {
            return clazzName + operation;
        }

        return clazzName + operation + parameters.size();
    }
    public String getClazzName() {
        return this.clazzName;
    }

    public String getOperation() {
        return this.operation;
    }

    public String getAdditionalSignature() {
        return additionalSignature;
    }

    public String getActualType() {
        return actualType;
    }

    public List<String> getParameters() {
        return this.parameters;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("{");
        builder.append("clazzName=").append(clazzName);
        builder.append(", operation=").append(operation);
        builder.append(", parameterTypes=").append(parameterTypes);
        builder.append(", additionalSignature=").append(additionalSignature);
        builder.append(", actualType=").append(actualType);
        builder.append('}');
        return builder.toString();
    }
}
