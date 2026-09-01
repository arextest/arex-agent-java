package io.arex.agent.compare.model.log;

import java.util.Objects;

public class NodeEntity {

    private String nodeName;
    private int index;

    public NodeEntity() {

    }

    public NodeEntity(String nodeName, int index) {
        this.nodeName = nodeName;
        this.index = index;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + (nodeName != null ? nodeName.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NodeEntity)) {
            return false;
        }

        NodeEntity that = (NodeEntity) obj;
        if (Objects.equals(this.getNodeName(), that.getNodeName())
                && this.getIndex() == that.getIndex()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {

        if (this.nodeName != null) {
            return nodeName;
        } else {
            return "[" + index + "]";
        }
    }
}
