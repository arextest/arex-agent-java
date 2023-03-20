package io.arex.agent.bootstrap.cache;

import io.arex.agent.bootstrap.internal.Pair;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedModuleCache {
    private static final Map<String, Pair<Integer, Integer>> PACKAGE_CACHE = new ConcurrentHashMap<>();

    public static void registerProjectModule(String moduleName, Pair<Integer, Integer> version) {
        if (moduleName == null || version == null) {
            return;
        }
        PACKAGE_CACHE.put(moduleName, version);
    }

    public static Pair<Integer, Integer> get(String moduleName) {
        return PACKAGE_CACHE.get(moduleName);
    }
}
