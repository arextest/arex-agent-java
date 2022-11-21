package io.arex.foundation.model;


import java.util.ArrayList;
import java.util.List;

/**
 * DynamicClassEntity
 */
public class DynamicClassEntity {

    private final String clazzName;
    private final String operation;
    private final String parameterTypes;
    private final String keyFormula;
    private final String returnType;
    private List<String> parameters;
    public DynamicClassEntity(String clazzName, String operation, String parameterTypes, String keyFormula) {
        this.clazzName = clazzName;
        this.operation = operation;
        this.parameterTypes = parameterTypes;

        if (parameterTypes != null && parameterTypes.length() > 0 && !"null".equals(parameterTypes)) {
            String[] types = parameterTypes.split("@");
            this.parameters = new ArrayList<>(types.length);
            for (int i = 0; i < types.length; i++) {
                if (types[i] != null && types[i].length() > 0) {
                    this.parameters.add(types[i]);
                }
            }
        }

        // fix dynamic response type is generic type
        this.keyFormula = keyFormula;
        this.returnType = null;
    }

    public String getClazzName() {
        return this.clazzName;
    }

    public String getOperation() {
        return this.operation;
    }

    public String getKeyFormula() {
        return keyFormula;
    }

    public String getReturnType() {
        return returnType;
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
        builder.append(", keyFormula=").append(keyFormula);
        builder.append(", returnType=").append(returnType);
        builder.append('}');
        return builder.toString();
    }
}
