package io.arex.foundation.extension;

import io.arex.api.Mode;
import io.arex.foundation.util.SPIUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ExtensionLoader
 */
public class ExtensionLoader<T> {
    private static final ConcurrentMap<Class<?>, Object> extensionInstances = new ConcurrentHashMap<>(64);
    private static final String MODE = System.getProperty("arex.storage.mode", "default");

    public static <T extends Mode> T getExtension(Class<T> type) {
        T extension = (T) extensionInstances.get(type);

        if (extension != null) {
            return extension;
        }

        List<T> extensionClassList = SPIUtil.load(type);
        if (extensionClassList.isEmpty()) {
            return (T) extensionInstances.put(type, null);
        }

        T defaultExtension = null;

        for (T instance : extensionClassList) {
            if (MODE.equals(instance.getMode())) {
                extension = instance;
                break;
            }

            if ("default".equals(instance.getMode())) {
                defaultExtension = instance;
            }
        }

        if (extension == null) {
            extension = defaultExtension;
        }

        return extension;
    }
}
