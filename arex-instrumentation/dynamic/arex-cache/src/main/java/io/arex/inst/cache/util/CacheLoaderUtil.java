package io.arex.inst.cache.util;

import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.log.LogManager;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheLoaderUtil {
    private static final Map<Integer, Field> REFERENCE_FIELD_MAP = new ConcurrentHashMap<>();
    private static final Map<Integer, String> NO_REFERENCE_MAP = new ConcurrentHashMap<>();
    private static final String LAMBDA_SUFFIX = "$$";
    private static final String EXTERNAL_VARIABLE_REFERENCE_IDENTIFIER = "val$";
    private static final String EXTERNAL_INSTANCE_REFERENCE_IDENTIFIER = "this$0";

    /**
     * Cache loader is mainly divided into three scenarios:
     * 1. public static final LoadingCache<String, Object> cacheStaticData = CacheBuilder.newBuilder().build(new CacheLoader<String, Object>() {...});
     * In this case, we can directly obtain the cache loader class name to distinguish the cache loader.
     * <p>
     * 2. public static final LoadingCache<String, Object> fatherCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Object>() {abstract load() {...}});
     * <p>
     * childCache1 extends fatherCache, childCache2 extends fatherCache. and override the load method.
     * In this case, we need to obtain the childCache1 or childCache2 class name to distinguish the cache loader.
     * the method parameter is the fatherCache, we can get the childCache1 or childCache2 class name from field this$0.
     * <p>
     * 3. public createLoadingCache(Object task) {CacheBuilder.newBuilder().build(new CacheLoader<String, Object>() {task.load()});
     * In this case, we need to obtain the task class name to distinguish the cache loader.
     * the method parameter is the cacheLoader, we can get the task class name from field val$task.
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
        Field field = REFERENCE_FIELD_MAP.computeIfAbsent(loaderHashCode, k -> getReferenceField(loaderClass));
        if (field == null) {
            return generateNameWithNoReference(loaderHashCode, loaderClass);
        }

        String className = getReferenceClass(field, loader);
        if (StringUtil.startWith(field.getName(), EXTERNAL_VARIABLE_REFERENCE_IDENTIFIER)) {
            NO_REFERENCE_MAP.put(loaderHashCode, className);
        }
        return className;
    }

    /**
     * cache loader without external references directly obtain class name, and remove lambda suffix.
     * @return
     */
    private static String generateNameWithNoReference(int loaderHashCode, Class<?> loaderClass) {
        String loaderClassName = StringUtil.substringBefore(loaderClass.getName(), LAMBDA_SUFFIX);
        NO_REFERENCE_MAP.put(loaderHashCode, loaderClassName);
        return loaderClassName;
    }

    /**
     * get the reference field of the loader.
     * the external traversal reference field has the highest priority.
     * If it is recognized, it can be returned. If not, all fields will be traversed.
     * ex:
     * fields [this$0, val$xx] return field[1] -> val$xx.
     * fields [this$0, xx] return field[0]-> this$0.
     * else return null.
     */
    private static Field getReferenceField(Class<?> loaderClass) {
        try {
            Field referenceField = null;
            for (Field field : loaderClass.getDeclaredFields()) {
                if (StringUtil.startWith(field.getName(), EXTERNAL_VARIABLE_REFERENCE_IDENTIFIER)) {
                    referenceField = field;
                    referenceField.setAccessible(true);
                    return referenceField;
                }
                if (StringUtil.equals(field.getName(), EXTERNAL_INSTANCE_REFERENCE_IDENTIFIER)) {
                    referenceField = field;
                    referenceField.setAccessible(true);
                }
            }
            return referenceField;
        } catch(Exception e) {
            LogManager.warn("CacheLoaderUtil.getReferenceField", e);
            return null;
        }
    }

    private static String getReferenceClass(Field field, Object cacheLoader) {
        try {
            Object referenceObject = field.get(cacheLoader);
            if (referenceObject == null) {
                return cacheLoader.getClass().getName();
            }
            return referenceObject.getClass().getName();
        } catch(Exception e) {
            LogManager.warn("CacheLoaderUtil.getReferenceClass", e);
            return cacheLoader.getClass().getName();
        }
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
