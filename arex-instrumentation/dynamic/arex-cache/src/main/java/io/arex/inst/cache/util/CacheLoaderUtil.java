package io.arex.inst.cache.util;

import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.log.LogManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheLoaderUtil {
    private static final Map<Integer, Field> REFERENCE_FIELD_MAP = new ConcurrentHashMap<>();
    private static final Map<Integer, String> NO_REFERENCE_MAP = new ConcurrentHashMap<>();

    /**
     * return the reference to the outer class in an inner class, if exists.
     * else return the class name of the loader.
     */
    public static String getLocatedClass(Object loader) {
        if (loader == null) {
            return StringUtil.EMPTY;
        }
        int loaderHashCode = System.identityHashCode(loader);
        String locatedClassName = NO_REFERENCE_MAP.get(loaderHashCode);
        if (locatedClassName != null) {
            return locatedClassName;
        }

        Class<?> loaderClass = loader.getClass();
        if (isNotAbstractOrInterface(loaderClass.getEnclosingClass())) {
            String loaderClassName = loaderClass.getName();
            NO_REFERENCE_MAP.put(loaderHashCode, loaderClassName);
            return loaderClassName;
        }

        Field field = REFERENCE_FIELD_MAP.computeIfAbsent(loaderHashCode, k -> getReferenceField(loaderClass));
        if (field == null) {
            String loaderClassName = loaderClass.getName();
            NO_REFERENCE_MAP.put(loaderHashCode, loaderClassName);
            return loaderClassName;
        }

        try {
            Object referenceObject = field.get(loader);
            return getReferenceClass(loader, referenceObject);
        } catch(Exception e) {
            LogManager.warn("CacheLoaderUtil.getLocatedClass", e);
            return loaderClass.getName();
        }
    }

    private static boolean isNotAbstractOrInterface(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        int modifiers = clazz.getModifiers();
        return !Modifier.isAbstract(modifiers) && !clazz.isInterface();
    }

    private static Field getReferenceField(Class<?> loaderClass) {
        try {
            Field referenceField = loaderClass.getDeclaredField("this$0");
            referenceField.setAccessible(true);
            return referenceField;
        } catch(Exception e) {
            LogManager.warn("CacheLoaderUtil.getReferenceField", e);
            return null;
        }
    }

    private static String getReferenceClass(Object cacheLoader, Object referenceObject) {
        if (referenceObject == null) {
            return cacheLoader.getClass().getName();
        }
        return referenceObject.getClass().getName();
    }

    public static boolean needRecordOrReplay(Object cacheLoader) {
        if (cacheLoader == null) {
            return false;
        }
        String[] coveragePackages = Config.get().getCoveragePackages();
        if (ArrayUtils.isEmpty(coveragePackages)){
            return false;
        }
        String loaderClassName = MapUtils.getString(NO_REFERENCE_MAP, System.identityHashCode(cacheLoader), cacheLoader.getClass().getName());
        for (String packageName : coveragePackages) {
            if (StringUtil.startWith(loaderClassName, packageName)) {
                return true;
            }
        }
        return false;
    }
}
