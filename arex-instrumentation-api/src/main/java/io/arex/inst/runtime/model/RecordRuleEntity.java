package io.arex.inst.runtime.model;

import java.util.List;

public class RecordRuleEntity {
    private String id;
    private String appId;
    private String httpPath;
    private List<ParamRuleEntity> paramRuleEntityList;

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

    public String getHttpPath() {
        return httpPath;
    }

    public void setHttpPath(String httpPath) {
        this.httpPath = httpPath;
    }

    public List<ParamRuleEntity> getParamRuleEntityList() {
        return paramRuleEntityList;
    }

    public void setParamRuleEntityList(List<ParamRuleEntity> paramRuleEntityList) {
        this.paramRuleEntityList = paramRuleEntityList;
    }
}
