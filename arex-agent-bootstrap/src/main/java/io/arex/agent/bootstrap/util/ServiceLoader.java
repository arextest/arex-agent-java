package io.arex.agent.bootstrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.ArrayList;
import java.util.List;

public class ServiceLoader {
    protected static final Map<String, List<String>> SERVICE_CACHE = new HashMap<>();
    private static final String PREFIX = "META-INF/services/";

    public static <T> List<T> load(Class<T> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return load(service, cl);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> load(Class<T> service, ClassLoader loader) {
        List<T> result = new ArrayList<>();
        final List<String> serviceList = SERVICE_CACHE.get(service.getName());
        if (CollectionUtil.isEmpty(serviceList)) {
            return result;
        }
        for (String className : serviceList) {
            try {
                final Class<?> serviceClazz = Class.forName(className, true, loader);
                result.add((T) serviceClazz.getDeclaredConstructor().newInstance());
            } catch (Throwable ex) {
                System.err.printf("Unable to load class: %s from classloader: %s, throwable: %s",
                        service.getName(), loader, ex.toString());
            }
        }
        return result;
    }

    /**
     * @param entryName ex: entryName: META-INF/services/io.arex.inst.runtime.serializer.StringSerializable
     * @param jarEntry  ex: jarEntry content:
     *                 io.arex.foundation.serializer.gson.GsonSerializer
     *                 io.arex.foundation.serializer.jackson.JacksonSerializer
     * SERVICE_CACHE: key: io.arex.inst.runtime.serializer.StringSerializable
     *                value: [io.arex.foundation.serializer.gson.GsonSerializer, io.arex.foundation.serializer.jackson.JacksonSerializer]
     */
    public static void buildCache(File file, JarEntry jarEntry, String entryName) {
        try(JarFile jarFile = new JarFile(file);
                InputStream inputStream = jarFile.getInputStream(jarEntry)) {
            List<String> serviceList = readAllLines(inputStream);
            if (CollectionUtil.isNotEmpty(serviceList)) {
                String className = entryName.substring(PREFIX.length());
                final List<String> list = SERVICE_CACHE.get(className);
                if (list == null) {
                    SERVICE_CACHE.put(className, serviceList);
                } else {
                    list.addAll(serviceList);
                }
            }
        } catch (Throwable ex) {
            System.err.printf("build spi map failed, file: %s%n", entryName);
        }
    }

    private static List<String> readAllLines(InputStream inputStream) {
        List<String> serviceList = new ArrayList<>();
        try(Reader isr = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isr)) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                serviceList.add(inputLine);
            }
        } catch (Throwable e) {
            System.err.print("read File failed.");
        }
        return serviceList;
    }

    public static boolean match(String name) {
        return name.startsWith(PREFIX);
    }
}
