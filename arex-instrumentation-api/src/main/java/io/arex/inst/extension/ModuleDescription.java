package io.arex.inst.extension;

import io.arex.agent.bootstrap.internal.Pair;

public class ModuleDescription {
    public static Builder builder() {
        return new Builder();
    }

    private Pair<Integer, Integer> from;
    private Pair<Integer, Integer> to;

    private String moduleName;

    private ModuleDescription(String moduleName,
                              Pair<Integer, Integer> supportFrom, Pair<Integer, Integer> supportTo) {
        this.moduleName = moduleName;
        this.from = supportFrom;
        this.to = supportTo;
    }

    public String getModuleName() {
        return moduleName;
    }

    public boolean isSupported(Pair<Integer, Integer> current) {
        boolean isSupported = current.getFirst() >= from.getFirst() && current.getSecond() >= from.getSecond();
        if (isSupported && to != null) {
            isSupported = current.getFirst() <= to.getFirst() && current.getSecond() <= to.getSecond();
        }

        return isSupported;
    }

    public static final class Builder {
        private String name;
        private Pair<Integer, Integer> from;
        private Pair<Integer, Integer> to;

        public Builder name(String moduleName) {
            this.name = moduleName;
            return this;
        }

        public Builder supportFrom(int major, int minor) {
            this.from = Pair.of(major, minor);
            return this;
        }

        public Builder supportTo(int major, int minor) {
            this.to = Pair.of(major, minor);
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
