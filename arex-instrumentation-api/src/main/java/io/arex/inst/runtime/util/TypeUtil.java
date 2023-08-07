package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
    public static final String DEFAULT_CLASS_NAME = "java.lang.String";
    private static final ConcurrentMap<String, Field> GENERIC_FIELD_CACHE = new ConcurrentHashMap<>();
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

        String[] types = StringUtil.splitByFirstSeparator(typeName, HORIZONTAL_LINE);

        try {
            Class<?> raw = classForName(types[0]);
            if (raw == null) {
                return null;
            }

            if (types.length > 1 && StringUtil.isNotEmpty(types[1])) {

                if (raw.getTypeParameters().length == 1) {
                    final Type[] args = new Type[]{forName(types[1])};
                    final ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(raw, args, null);
                    TYPE_NAME_CACHE.put(typeName, parameterizedType);
                    return parameterizedType;
                }

                if (raw.getTypeParameters().length == 2) {
                    final String[] split = StringUtil.splitByFirstSeparator(types[1], COMMA);
                    Type[] args = new Type[]{forName(split[0]), forName(split[1])};
                    ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(raw, args, null);
                    TYPE_NAME_CACHE.put(typeName, parameterizedType);
                    return parameterizedType;
                }
                TYPE_NAME_CACHE.put(typeName, raw);
                return raw;
            }
            TYPE_NAME_CACHE.put(typeName, raw);
            return raw;
        } catch (Throwable ex) {
            LogManager.warn("forName", ex);
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

        if (isGenericType(result)) {
            return genericTypeToString(result);
        }

        return result.getClass().getName();
    }

    private static String genericTypeToString(Object result) {
        final Class<?> rawClass = result.getClass();
        StringBuilder builder = new StringBuilder();
        String rawClassName = rawClass.getName();
        builder.append(rawClassName).append(HORIZONTAL_LINE);
        final Type[] typeParameters = rawClass.getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            final String typeName = typeParameters[i].getTypeName();
            String cacheKey = rawClassName + typeName;
            Field field = GENERIC_FIELD_CACHE.get(cacheKey);
            if (field == null) {
                for (Field declaredField : rawClass.getDeclaredFields()) {
                    // java.util.List<T> contains T
                    if (declaredField.getGenericType().getTypeName().contains(typeName)) {
                        declaredField.setAccessible(true);
                        GENERIC_FIELD_CACHE.put(cacheKey, declaredField);
                        field = declaredField;
                        break;
                    }
                }
            }
            builder.append(filterRawGenericType(invokeGetFieldType(field, result)));
            if (i == typeParameters.length - 1) {
               return builder.toString();
            }
            builder.append(COMMA);
        }
        return builder.toString();
    }

    private static String invokeGetFieldType(Field field, Object result) {
        if (field == null || result == null) {
            return null;
        }
        try {
            final Object genericField = field.get(result);
            return getName(genericField);
        } catch (Throwable ex) {
            LogManager.warn("invokeGetFieldType", ex);
            return null;
        }
    }

    private static boolean isGenericType(Object result) {
        return result.getClass().getTypeParameters().length != 0;
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

            if (!(innerObj instanceof List)) {
                builder.append(getName(innerObj));
                return builder.toString();
            }

            String innerObjClassName = innerObj.getClass().getName();
            if (!linkedList.contains(innerObjClassName)) {
                linkedList.add(innerObjClassName);
            }

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
            String valueClassName = entry.getValue() == null ? DEFAULT_CLASS_NAME : getName(entry.getValue());
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
            LogManager.warn("classForName", ex);
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

    public static String errorSerializeToString(Object object) {
        if (object instanceof Object[]) {
            Object[] objects = (Object[]) object;
            StringBuilder builder = new StringBuilder();
            for (Object obj : objects) {
                builder.append(obj.getClass().getName()).append(",");
            }
            return builder.toString();
        }
        return getName(object);
    }

    /**
     * filter out effective raw generic in collection: A<List<T>> -> A<T>
     * class A<T> {
     *     List<T> t;
     * }
     * otherwise deserialize will fail
     * such as: com.xxx.Response-java.util.ArrayList-java.lang.String
     * return com.xxx.Response-java.lang.String
     */
    private static String filterRawGenericType(String genericType) {
        if (StringUtil.isEmpty(genericType)) {
            // does not return null, otherwise com.xxx.Response-null will deserialize throw ClassNotFoundException: null
            // this case means that the generic field is not assigned a value
            return StringUtil.EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        String[] types = StringUtil.split(genericType, HORIZONTAL_LINE);
        for (String type : types) {
            if (StringUtil.isNullWord(type) || isCollection(type)) {
                continue;
            }
            builder.append(HORIZONTAL_LINE).append(type);
        }
        return builder.substring(1);
    }

    private static boolean isCollection(String genericType) {
        try {
            Class<?> clazz = Class.forName(genericType);
            return Collection.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
