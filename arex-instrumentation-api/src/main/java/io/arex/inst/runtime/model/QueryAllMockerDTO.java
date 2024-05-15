package io.arex.inst.runtime.model;

import java.util.Arrays;

public class QueryAllMockerDTO {
    private String recordId;
    private String[] fieldNames =new String[]{"id", "categoryType", "operationName", "targetRequest", "targetResponse", "creationTime"};
    private String[] categoryTypes;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }

    public String[] getCategoryTypes() {
        return categoryTypes;
    }

    public void setCategoryTypes(String[] categoryTypes) {
        this.categoryTypes = categoryTypes;
    }

    public String replayLogTitle() {
        return "replay.allMocker";
    }

    @Override
    public String toString() {
        return "{" +
                "recordId='" + recordId + '\'' +
                ", fieldNames=" + Arrays.toString(fieldNames) +
                ", categoryTypes=" + Arrays.toString(categoryTypes) +
                '}';
    }
}
