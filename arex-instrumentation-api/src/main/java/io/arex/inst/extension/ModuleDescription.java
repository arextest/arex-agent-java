package io.arex.inst.extension;

import io.arex.agent.bootstrap.model.ComparableVersion;

public class ModuleDescription {
    public static Builder builder() {
        return new Builder();
    }

    private ComparableVersion from;
    private ComparableVersion to;

    private String moduleName;

    private ModuleDescription(String moduleName,
                              ComparableVersion supportFrom, ComparableVersion supportTo) {
        this.moduleName = moduleName;
        this.from = supportFrom;
        this.to = supportTo;
    }

    public String getModuleName() {
        return moduleName;
    }

    public boolean isSupported(ComparableVersion current) {
        boolean isSupported = current.compareTo(from) >= 0;
        if (isSupported && to != null) {
            isSupported = current.compareTo(to) <= 0;
        }
        return isSupported;
    }

    public static final class Builder {
        private String name;
        private ComparableVersion from;
        private ComparableVersion to;

        public Builder name(String moduleName) {
            this.name = moduleName;
            return this;
        }

        public Builder supportFrom(ComparableVersion version) {
            this.from = version;
            return this;
        }

        public Builder supportTo(ComparableVersion version) {
            this.to = version;
            return this;
        }

        public ModuleDescription build() {
            if (name == null || from == null) {
                return null;
            }
            return new ModuleDescription(name, from, to);
        }
    }
}
