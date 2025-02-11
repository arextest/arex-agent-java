package io.arex.inst.extension;

import io.arex.agent.bootstrap.model.ComparableVersion;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.CollectionUtil;
import java.util.HashSet;
import java.util.Set;

public class ModuleDescription {
    public static Builder builder() {
        return new Builder();
    }

    private ComparableVersion from;
    private ComparableVersion to;

    private Set<String> moduleNames;

    private ModuleDescription(Set<String> moduleNames,
                              ComparableVersion supportFrom, ComparableVersion supportTo) {
        this.moduleNames = moduleNames;
        this.from = supportFrom;
        this.to = supportTo;
    }

    public Set<String> getModuleNames() {
        return moduleNames;
    }

    public boolean isSupported(ComparableVersion current) {
        boolean isSupported = current.compareTo(from) >= 0;
        if (isSupported && to != null) {
            isSupported = current.compareTo(to) <= 0;
        }
        return isSupported;
    }

    public static final class Builder {
        private Set<String> names;
        private ComparableVersion from;
        private ComparableVersion to;

        public Builder name(String... moduleNames) {
            if (ArrayUtils.isNotEmpty(moduleNames)) {
                names = new HashSet<>(moduleNames.length);
                for (String name : moduleNames) {
                    names.add(name);
                }
            }
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
            if (CollectionUtil.isEmpty(names) || from == null) {
                return null;
            }
            return new ModuleDescription(names, from, to);
        }
    }
}
