package io.arex.agent.bootstrap.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedModuleCache {
    private static final Map<String, String> PACKAGE_CACHE = new ConcurrentHashMap<>();

    public static void registerProjectModule(String moduleName, String version) {
        if (moduleName == null || version == null) {
            return;
        }
        PACKAGE_CACHE.put(moduleName, version);
    }

    public static String get(String moduleName) {
        return PACKAGE_CACHE.get(moduleName);
    }

    public static boolean exist(String moduleName) {
        return PACKAGE_CACHE.containsKey(moduleName);
    }
}
