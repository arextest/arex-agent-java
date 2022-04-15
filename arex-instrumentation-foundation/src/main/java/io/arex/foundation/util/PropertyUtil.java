package io.arex.foundation.util;

import java.io.InputStream;
import java.util.*;

public class PropertyUtil {
    private static final HashMap<String, String> PROPERTY_MAP = new HashMap<>();
    private static final List<String> PROPERTIES_FILE_LIST = new ArrayList<>();
    private static final List<Properties> PROPERTIES_LIST = new ArrayList<>();

    static {
        fillPropertyFileList();
        loadProperties();
        propertiesToMap();
    }

    public static String getProperty(String key) {
        return PROPERTY_MAP.get(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return PROPERTY_MAP.getOrDefault(key, defaultValue);
    }

    private static void propertiesToMap() {
        for (Properties properties : PROPERTIES_LIST) {
            properties.stringPropertyNames().forEach(name -> PROPERTY_MAP.put(name, properties.getProperty(name)));
        }
    }

    private static void loadProperties() {
        for (String file : PROPERTIES_FILE_LIST) {
            try (InputStream inputStream = PropertyUtil.class.getClassLoader().getResourceAsStream(file)) {
                Properties props = new Properties();
                props.load(inputStream);
                PROPERTIES_LIST.add(props);
            } catch (Throwable e) {
                LogUtil.warn("loadProperties:" + file, e);
            }
        }
    }

    private static void fillPropertyFileList() {
        PROPERTIES_FILE_LIST.add("arex-instrumentation-foundation.properties");
    }

}
