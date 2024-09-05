package io.arex.inst.runtime.model;

import java.util.List;

public class ParamRuleEntity {
    private String appId;
    private String urlRuleId;
    private String paramRuleId;
    /**
     * 参数类型枚举：url末尾参数:QUERY_STRING, body参数JSON格式:JSON_BODY
     */
    private String paramType;
    private List<ValueRuleEntity> valueRuleEntityList;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUrlRuleId() {
        return urlRuleId;
    }

    public void setUrlRuleId(String urlRuleId) {
        this.urlRuleId = urlRuleId;
    }

    public String getParamRuleId() {
        return paramRuleId;
    }

    public void setParamRuleId(String paramRuleId) {
        this.paramRuleId = paramRuleId;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public List<ValueRuleEntity> getValueRuleEntityList() {
        return valueRuleEntityList;
    }

    public void setValueRuleEntityList(List<ValueRuleEntity> valueRuleEntityList) {
        this.valueRuleEntityList = valueRuleEntityList;
    }
}
