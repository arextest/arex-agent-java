package io.arex.inst.runtime.model;

import java.util.List;

public class RecordRuleEntity {
    private String appId;
    private String urlRuleId;
    private String httpPath;
    private List<ParamRuleEntity> paramRuleEntityList;

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
