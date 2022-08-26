package io.arex.agent.bootstrap.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoadedModuleCache {
    private static final Set<String> modules = new HashSet<>(50);

    public static boolean hasResource(List<String> packages) {
        if (packages == null || packages.size() == 0 || modules.size() == 0) {
            return true;
        }

        for (String module : packages) {
            if (modules.contains(module)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Register a package that allows instrumentation
     *
     * @param packageName package name in manifest file, with key: Bundle-Name or Automatic-Module-Name
     * @param version package version in manifest file, with key: Bundle-Version or Implementation-Version
     */
    public static void registerResource(String packageName, String version) {
        modules.add(toModule(packageName, version));
    }

    public static String toModule(String packageName, String version) {
        if (packageName == null || version == null) {
            return "";
        }

        int index = version.indexOf('.');
        return packageName + "-" + (index < 0 ? version : version.substring(0, index));
    }
}
