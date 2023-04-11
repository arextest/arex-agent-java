package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

public class TypeUtil {

    public static final char COMMA = ',';
    public static final char HORIZONTAL_LINE = '-';
    public static final String HORIZONTAL_LINE_STR = "-";
    public static final String SEMICOLON = ";";
    public static final String DEFAULT_CLASS_NAME = "java.lang.String";
    private static final String MAP_CLASS_NAME = "HashMap";
    private static final String OPTIONAL_CLASS_NAME = "java.util.Optional";
    private static final String JAVA_UTIL_PACKAGE_NAME = "java.util";
    private static final String LIST_CLASS_NAME = "List";

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeUtil.class);
    private static final ConcurrentMap<String, Type> TYPE_NAME_CACHE = new ConcurrentHashMap<>();
    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private TypeUtil() {}

    public static Type forName(String typeName) {
        if (StringUtil.isEmpty(typeName) || HORIZONTAL_LINE_STR.equals(typeName)) {
            return null;
        }

        Type type = TYPE_NAME_CACHE.get(typeName);
        if (type != null) {
            return type;
        }

        String[] types = StringUtil.split(typeName, HORIZONTAL_LINE);

        if (ArrayUtils.isEmpty(types)) {
            return null;
        }

        try {
            Class<?> raw = classForName(types[0]);

            if (types.length > 1 && StringUtil.isNotEmpty(types[1])) {
                if (raw == null) {
                    return null;
                }

                if (raw.getTypeParameters().length == 0) {
                    ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(raw, new Type[0], null);
                    TYPE_NAME_CACHE.put(typeName, parameterizedType);
                    return parameterizedType;
                }

                // List<Map>
                if (types[1].contains(MAP_CLASS_NAME)) {
                    Type[] args = getMapType(types);
                    ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(raw, args, null);
                    TYPE_NAME_CACHE.put(typeName, parameterizedType);
                    return parameterizedType;
                }

                // Only support Optional<List<entity>> type; types:type[0]:java.util.Optional,type[1]:java.util.ArrayList,type[2]:entityClassName
                if (OPTIONAL_CLASS_NAME.equals(types[0]) && types[1].contains(JAVA_UTIL_PACKAGE_NAME)
                    && types[1].contains(LIST_CLASS_NAME)) {
                    Type[] args = getListType(types);
                    ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(raw, args, null);
                    TYPE_NAME_CACHE.put(typeName, parameterizedType);
                    return parameterizedType;
                }

                types = StringUtil.split(types[1], COMMA);
                Type[] args = new Type[types.length];
                for (int i = 0; i < types.length; i++) {
                    args[i] = classForName(types[i]);
                }
                ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(raw, args, null);
                TYPE_NAME_CACHE.put(typeName, parameterizedType);
                return parameterizedType;
            } else {
                TYPE_NAME_CACHE.put(typeName, raw);
                return raw;
            }
        } catch (Throwable ex) {
            LOGGER.warn(LogUtil.buildTitle("forName"), ex);
            return null;
        }
    }

    public static String getName(Object result) {
        if (result == null) {
            return null;
        }

        if (result instanceof Map) {
            return mapToString((Map<?, ?>) result);
        }

        // Optional GSON
        if (result instanceof Optional) {
            return optionalToString((Optional<?>) result);
        }

        if (result instanceof Collection<?>) {
            return collectionToString((Collection<?>) result);
        }

        if (result instanceof ParameterizedType) {
            return parameterizedTypeToString((ParameterizedType) result);
        }

        if (result instanceof Class) {
            return ((Class<?>) result).getTypeName();
        }

        return result.getClass().getName();
    }


    /**
     * Get raw class of type
     */
    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        }

        return null;
    }

    /**
     * Only support parsing type from {@code List<Entity>[0]}
     *
     */
    public static String getListMapName(List<Map<?, ?>> sourceListMap) {
        if (sourceListMap == null) {
            return null;
        }
        if (sourceListMap.isEmpty()) {
            return sourceListMap.getClass().getName();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(sourceListMap.getClass().getName()).append(HORIZONTAL_LINE);

        for (Map<?, ?> map : sourceListMap) {
            if (map.size() < 1) {
                return builder.append(map.getClass().getName()).toString();
            }
            builder.append(mapToString(map));
            break;
        }
        return builder.toString();
    }

    /**
     * only support {@code List<Object> and List<List<Object>>} type
     */
    private static String collectionToString(Collection<?> result) {
        StringBuilder builder = new StringBuilder();
        builder.append(result.getClass().getName());

        if (result.isEmpty()) {
            return builder.toString();
        }

        builder.append(HORIZONTAL_LINE);

        List<String> linkedList = new LinkedList<>();

        for (Object innerObj : result) {
            if (innerObj == null) {
                continue;
            }

            String innerObjClassName = innerObj.getClass().getName();
            if (!linkedList.contains(innerObjClassName)) {
                linkedList.add(innerObjClassName);
            }

            if (innerObj instanceof List) {
                List<?> innerList = (List<?>) innerObj;
                for (Object innerElement : innerList) {
                    if (innerElement == null) {
                        continue;
                    }

                    String innerElementClassName = innerElement.getClass().getName();

                    // By default, the data types in list<list<Object,Object>> are the same, and the inner list gets the first non-null element, which is break.
                    linkedList.add(innerElementClassName);
                    break;
                }
            } else {
                break;
            }
        }
        builder.append(StringUtil.join(linkedList, ","));
        return builder.toString();
    }

    private static String optionalToString(Optional<?> result) {
        StringBuilder builder = new StringBuilder();
        builder.append(Optional.class.getName());
        result.ifPresent(o -> builder.append(HORIZONTAL_LINE).append(getName(o)));
        return builder.toString();
    }

    private static String mapToString(Map<?, ?> result) {
        if (result.size() < 1) {
            return result.getClass().getName();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(result.getClass().getName()).append(HORIZONTAL_LINE);
        for (Map.Entry<?, ?> entry : result.entrySet()) {
            String keyClassName = entry.getKey() == null ? DEFAULT_CLASS_NAME : entry.getKey().getClass().getName();
            String valueClassName =
                entry.getValue() == null ? DEFAULT_CLASS_NAME : entry.getValue().getClass().getName();
            builder.append(keyClassName).append(COMMA).append(valueClassName);
            break;
        }
        return builder.toString();
    }

    private static Class<?> classForName(String type) {
        try {
            if (Thread.currentThread().getContextClassLoader() == null) {
                return Class.forName(type);
            }
            return Class.forName(type, false, Thread.currentThread().getContextClassLoader());
        } catch (Throwable ex) {
            LOGGER.warn(LogUtil.buildTitle("classForName"), ex);
            return null;
        }
    }

    /**
     * Currently only support {@code A<B.class>} type
     */
    private static String parameterizedTypeToString(ParameterizedType type) {
        StringBuilder builder = new StringBuilder();
        if (type.getRawType() != null) {
            builder.append(type.getRawType().getTypeName());
        }
        if (ArrayUtils.isNotEmpty(type.getActualTypeArguments()) && type.getActualTypeArguments()[0] != null) {
            builder.append(TypeUtil.HORIZONTAL_LINE).append(type.getActualTypeArguments()[0].getTypeName());
        }
        return builder.toString();
    }

    /**
     * Get Map type[]
     *
     * {@code List<Map<String, String>> -> Type[] {Map, String, String}}
     */
    private static Type[] getMapType(String[] types) {
        try {
            Class<?> innerMapRaw = Class.forName(types[1], false, Thread.currentThread().getContextClassLoader());
            if (types.length > 2 && StringUtil.isNotEmpty(types[2])) {
                types = StringUtil.split(types[2], COMMA);
                Type[] args = new Type[types.length];
                for (int i = 0; i < types.length; i++) {
                    args[i] = Class.forName(types[i], false, Thread.currentThread().getContextClassLoader());
                }
                ParameterizedTypeImpl tempType = ParameterizedTypeImpl.make(innerMapRaw, args, null);
                return new Type[]{tempType};
            }
            return new Type[]{innerMapRaw};
        } catch (Throwable ex) {
            LOGGER.warn(LogUtil.buildTitle("getListMapType"), ex);
            return null;
        }
    }

    /**
     * only support {@code List<entity>} type
     */
    private static Type[] getListType(String[] types) {
        try {
            Class<?> innerListRaw = classForName(types[1]);
            if (types.length > 2 && StringUtil.isNotEmpty(types[2])) {
                ParameterizedTypeImpl tempType = ParameterizedTypeImpl.make(innerListRaw,
                    new Type[]{classForName(types[2])}, null);
                return new Type[]{tempType};
            }
            return new Type[]{innerListRaw};
        } catch (Throwable ex) {
            LOGGER.warn(LogUtil.buildTitle("getListType"), ex);
            return null;
        }
    }
}
