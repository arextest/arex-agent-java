package io.arex.foundation.services;

import java.util.HashSet;
import java.util.Set;

public class IgnoreService {

    private static Set<String> ignore = new HashSet<>();
    private static Set<String> ignoreModules = new HashSet<>();

    static {
        ignore.add("POST /artemis-discovery-service/api/discovery/lookup.json");
        ignore.add("POST /api/CMSGetServer");
    }

    /**
     * Enable module instrumentation by config
     */
    public static boolean isModuleEnabled(String module) {
        return !ignoreModules.contains(module);
    }

    /**
     * Enable the target mock by config
     * default return true
     */
    public static boolean isTargetMockEnabled(String targetName) {
        return !ignore.contains(targetName);
    }
}
