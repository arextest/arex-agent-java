package io.arex.inst.runtime.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class SPIUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SPIUtil.class);

    public static <T> List<T> load(Class<T> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return load(service, cl);
    }

    @SuppressWarnings("ForEachIterable")
    public static <T> List<T> load(Class<T> service, ClassLoader loader) {
        List<T> result = new ArrayList<>();
        ServiceLoader<T> services = ServiceLoader.load(service, loader);
        for (Iterator<T> iter = services.iterator(); iter.hasNext(); ) {
            try {
                result.add(iter.next());
            } catch (Throwable e) {
                LOGGER.warn("Unable to load class: {} from classloader: {}, throwable: {}",
                    service.getName(), service.getClassLoader(), e.toString());
            }
        }
        return result;
    }
}
