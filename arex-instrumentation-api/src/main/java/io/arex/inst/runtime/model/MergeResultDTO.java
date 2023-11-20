package io.arex.inst.runtime.model;

public class MergeResultDTO {
    private String category;
    private int methodSignatureKey;
    private String clazzName;
    private String methodName;
    private Object[] args;
    private Object result;
    private String resultClazz;
    private String serializeType;

    public MergeResultDTO() {}

    private MergeResultDTO(String category, String clazzName, String methodName, Object[] args, Object result,
                           String resultClazz, int methodSignatureKey, String serializeType) {
        this.category = category;
        this.clazzName = clazzName;
        this.methodName = methodName;
        this.args = args;
        this.result = result;
        this.resultClazz = resultClazz;
        this.methodSignatureKey = methodSignatureKey;
        this.serializeType = serializeType;
    }

    public static MergeResultDTO of(String category, String clazzName, String methodName, Object[] args,
                                    Object result, String resultClazz, int methodSignatureKey, String serializeType) {
        return new MergeResultDTO(category, clazzName, methodName, args, result, resultClazz, methodSignatureKey, serializeType);
    }

    public int getMethodSignatureKey() {
        return methodSignatureKey;
    }

    public void setMethodSignatureKey(int methodSignatureKey) {
        this.methodSignatureKey = methodSignatureKey;
    }

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getResultClazz() {
        return resultClazz;
    }

    public void setResultClazz(String resultClazz) {
        this.resultClazz = resultClazz;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(String serializeType) {
        this.serializeType = serializeType;
    }
}
