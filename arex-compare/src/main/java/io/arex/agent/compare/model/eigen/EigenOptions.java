package io.arex.agent.compare.model.eigen;

import java.util.Collection;
import java.util.HashSet;
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

    public static EigenOptions options() {
        return new EigenOptions();
    }

    public EigenOptions putCategoryType(String categoryType) {
        this.categoryType = categoryType;
        return this;
    }

    public EigenOptions putIgnoreNodes(String nodeName) {
        if (nodeName == null || nodeName.isEmpty()) {
            return this;
        }
        if (this.ignoreNodes == null) {
            this.ignoreNodes = new HashSet<>();
        }
        this.ignoreNodes.add(nodeName);
        return this;
    }

    public EigenOptions putIgnoreNodes(Collection<String> nodeNames) {
        if (nodeNames == null || nodeNames.isEmpty()) {
            return this;
        }
        if (this.ignoreNodes == null) {
            this.ignoreNodes = new HashSet<>();
        }
        this.ignoreNodes.addAll(nodeNames);
        return this;
    }

    public EigenOptions putExclusions(List<String> path) {
        if (path == null || path.isEmpty()) {
            return this;
        }
        if (this.exclusions == null) {
            this.exclusions = new HashSet<>();
        }
        this.exclusions.add(path);
        return this;
    }

    public EigenOptions putExclusions(Collection<List<String>> paths) {
        if (paths == null || paths.isEmpty()) {
            return this;
        }
        if (this.exclusions == null) {
            this.exclusions = new HashSet<>();
        }
        this.exclusions.addAll(paths);
        return this;
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
