package io.arex.inst.runtime.model;

import java.util.List;

public class ParamRuleEntity {
    private String id;
    private String appId;
    private String urlRuleId;
    private String paramType;
    private List<ValueRuleEntity> valueRuleEntityList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
