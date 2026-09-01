package io.arex.agent.compare.model.eigen;

import java.util.List;
import java.util.Set;

public class EigenOptions {

    private String categoryType;

    /**
     * the collection of the node name which is ignore
     */
    private Set<String> ignoreNodes;

    /**
     * the collection of the node path which is ignore
     */
    private Set<List<String>> exclusions;

    public EigenOptions() {
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public void setIgnoreNodes(Set<String> ignoreNodes) {
        this.ignoreNodes = ignoreNodes;
    }

    public void setExclusions(Set<List<String>> exclusions) {
        this.exclusions = exclusions;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public Set<List<String>> getExclusions() {
        return exclusions;
    }

    public Set<String> getIgnoreNodes() {
        return ignoreNodes;
    }
}
