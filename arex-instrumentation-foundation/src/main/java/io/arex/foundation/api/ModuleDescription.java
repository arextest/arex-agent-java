package io.arex.foundation.api;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;

import java.util.ArrayList;
import java.util.List;


public class ModuleDescription {

    public static Builder builder() {
        return new Builder();
    }

    private final List<String> packages;

    private ModuleDescription(List<String> packages) {
        this.packages = packages;
    }

    public List<String> getPackages() {
        return this.packages;
    }

    public boolean hasPackages() {
        return this.packages != null && this.packages.size() > 0;
    }

    public static final class Builder {
        private final List<String> packages = new ArrayList<>(2);

        /**
         * Register a package that allows instrumentation
         *
         * @param name  package name in manifest file, with key: Bundle-Name or Automatic-Module-Name
         * @param version package version in manifest file, with key: Bundle-Version or Implementation-Version
         */
        public Builder addPackage(String name, String version) {
            packages.add(LoadedModuleCache.toModule(name, version));
            return this;
        }

        public ModuleDescription build() {
            return new ModuleDescription(this.packages);
        }
    }
}
