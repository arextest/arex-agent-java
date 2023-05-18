package io.arex.agent.bootstrap.model;

import io.arex.agent.bootstrap.util.StringUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public interface Mocker extends Serializable {

    String getAppId();

    String getReplayId();

    void setReplayId(String var1);

    String getRecordId();

    void setRecordId(String var1);

    void setRecordEnvironment(int var1);

    String getRecordVersion();

    void setCreationTime(long var1);

    long getCreationTime();

    void setId(String var1);

    String getId();

    MockCategoryType getCategoryType();

    String getOperationName();

    Target getTargetRequest();

    Target getTargetResponse();

    public static class Target implements Serializable {

        private String body;
        private Map<String, Object> attributes;
        private String type;

        public Target() {
        }

        public Object getAttribute(String name) {
            return this.attributes == null ? null : this.attributes.get(name);
        }

        public void setAttribute(String name, Object value) {
            if (this.attributes == null) {
                this.attributes = new HashMap<>();
            }

            if (value == null) {
                this.attributes.remove(name);
            } else {
                this.attributes.put(name, value);
            }
        }

        public String attributeAsString(String name) {
            Object result = this.getAttribute(name);
            return result instanceof String ? (String) result : null;
        }

        public String getBody() {
            return this.body;
        }

        public Map<String, Object> getAttributes() {
            return this.attributes;
        }

        public String getType() {
            return this.type;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    default StringBuilder logBuilder() {
        StringBuilder builder = new StringBuilder("[arex]");
        boolean isReplay = StringUtil.isNotEmpty(getReplayId());
        if (isReplay) {
            builder.append("replay");
        } else {
            builder.append("record");
        }
        builder.append(" category: ").append(getCategoryType().getName());
        builder.append(", operation: ").append(getOperationName());
        builder.append(", recordId: ").append(getRecordId());
        if (isReplay) {
            builder.append(", replayId: ").append(getReplayId());
        }
        return builder;
    }
}
