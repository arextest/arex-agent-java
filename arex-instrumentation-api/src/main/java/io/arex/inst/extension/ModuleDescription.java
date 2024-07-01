package io.arex.inst.extension;

import io.arex.agent.bootstrap.model.ComparableVersion;
import io.arex.agent.bootstrap.util.CollectionUtil;
import java.util.Arrays;
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
        private final Set<String> names = new HashSet<>();
        private ComparableVersion from;
        private ComparableVersion to;

        public Builder name(String... moduleName) {
            if (moduleName != null && moduleName.length > 0) {
                this.names.addAll(Arrays.asList(moduleName));
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
