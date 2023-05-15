package io.arex.inst.runtime.model;

import io.arex.agent.bootstrap.util.StringUtil;
import java.util.Arrays;
import java.util.List;

public class SerializeSkipInfo {

    private String fullClassName;
    private String fieldName;

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<String> getFieldNameList() {
        return Arrays.asList(StringUtil.split(fieldName, ','));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SerializeSkipInfo && fullClassName.equals(
                ((SerializeSkipInfo) obj).getFullClassName()) && fieldName.equals(
                ((SerializeSkipInfo) obj).getFieldName());
    }

    @Override
    public int hashCode() {
        return fullClassName.hashCode() * 17 + fieldName.hashCode();
    }

    @Override
    public String toString() {
        return "fullClassName:" + fullClassName
                + ", fieldName:" + fieldName;
    }
}
