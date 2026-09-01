package io.arex.inst.runtime.model;

import java.util.List;
import java.util.Set;

public class CompareConfigurationEntity {

    private List<ConfigComparisonExclusionsEntity> comparisonExclusions;

    private Set<List<String>> globalExclusionList;

    private Set<String> ignoreNodeSet;

    public List<ConfigComparisonExclusionsEntity> getComparisonExclusions() {
        return comparisonExclusions;
    }

    public void setComparisonExclusions(List<ConfigComparisonExclusionsEntity> comparisonExclusions) {
        this.comparisonExclusions = comparisonExclusions;
    }

    public Set<List<String>> getGlobalExclusionList() {
        return globalExclusionList;
    }

    public void setGlobalExclusionList(Set<List<String>> globalExclusionList) {
        this.globalExclusionList = globalExclusionList;
    }

    public Set<String> getIgnoreNodeSet() {
        return ignoreNodeSet;
    }

    public void setIgnoreNodeSet(Set<String> ignoreNodeSet) {
        this.ignoreNodeSet = ignoreNodeSet;
    }

    public static class ConfigComparisonExclusionsEntity {
        private String operationName;
        private String categoryType;
        private Set<List<String>> exclusionList;

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public String getCategoryType() {
            return categoryType;
        }

        public void setCategoryType(String categoryType) {
            this.categoryType = categoryType;
        }

        public Set<List<String>> getExclusionList() {
            return exclusionList;
        }

        public void setExclusionList(Set<List<String>> exclusionList) {
            this.exclusionList = exclusionList;
        }
    }
}
