package io.arex.inst.runtime.util;

import io.arex.inst.runtime.config.Config;

public class PropertyUtil {
    /**
     * System.property > Config.get()
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(System.getProperty(key, Boolean.toString(Config.get().getBoolean(key, defaultValue))));
    }
}
