package io.arex.agent.compare.model;

import java.util.List;
import java.util.Set;

public class RulesConfig {
    private String categoryType;

    private String baseMsg;

    private List<List<String>> exclusions;

    private Set<String> ignoreNodeSet;

    private boolean nameToLower;

    public RulesConfig() {
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getBaseMsg() {
        return baseMsg;
    }

    public void setBaseMsg(String baseMsg) {
        this.baseMsg = baseMsg;
    }

    public List<List<String>> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<List<String>> exclusions) {
        this.exclusions = exclusions;
    }

    public Set<String> getIgnoreNodeSet() {
        return ignoreNodeSet;
    }

    public void setIgnoreNodeSet(Set<String> ignoreNodeSet) {
        this.ignoreNodeSet = ignoreNodeSet;
    }

    public boolean isNameToLower() {
        return nameToLower;
    }

    public void setNameToLower(boolean nameToLower) {
        this.nameToLower = nameToLower;
    }
}
