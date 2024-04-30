package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ParameterizedTypeImpl;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;

import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TypeUtil {

    public static final char COMMA = ',';
    public static final char HORIZONTAL_LINE = '-';
    public static final String HORIZONTAL_LINE_STR = "-";
    public static final String DEFAULT_CLASS_NAME = "java.lang.String";
    private static final ConcurrentMap<String, Field> GENERIC_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Type> TYPE_NAME_CACHE = new ConcurrentHashMap<>();
    private static final Class<?> DEFAULT_LIST_CLASS = List.class;
    private static final Class<?> DEFAULT_SET_CLASS = Set.class;
    private static final String PROTOCOL_BUFFER_PACKAGE = "com.google.protobuf";
    private static final String JODA_LOCAL_DATE_TIME = "org.joda.time.LocalDateTime";
    private static final String JODA_LOCAL_DATE = "org.joda.time.LocalDate";
    private static final String JODA_LOCAL_TIME = "org.joda.time.LocalTime";
    private static final String JODA_DATE_TIME = "org.joda.time.DateTime";
    private static final String METHOD_INVOCATION_PROCEEDING_JOIN_POINT = "org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint";
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
                int typeParametersLength = raw.getTypeParameters().length;
                if (typeParametersLength == 1) {
                    return forNameWithOneGenericType(raw, types[1], typeName);
                }

                if (typeParametersLength == 2) {
                    String[] split = StringUtil.splitByFirstSeparator(types[1], COMMA);
                    if (split[0].contains(HORIZONTAL_LINE_STR)) {
                        // ex: Pair-Map-String,String,Boolean
                        split = StringUtil.splitByLastSeparator(types[1], COMMA);
                    }
                    Type[] args = new Type[]{forName(split[0]), forName(split[1])};
                    ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(raw, args, null);
                    TYPE_NAME_CACHE.put(typeName, parameterizedType);
                    return parameterizedType;
                }

                if (needUseDefaultCollection(raw, types[1], typeParametersLength)) {
                    if (Set.class.isAssignableFrom(raw)) {
                        return forNameWithOneGenericType(DEFAULT_SET_CLASS, types[1], typeName);
                    }
                    return forNameWithOneGenericType(DEFAULT_LIST_CLASS, types[1], typeName);
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

    /**
     * If the parent class explicitly specifies generics,
     * the serialization framework will process it and not need use default collection.
     * eg: class WrappedList extends WrappedCollection implements List<V> return true
     * eg: class exampleCollection extends ArrayList<String> return false
     */
    private static boolean needUseDefaultCollection(Class<?> raw, String type, int typeParametersLength) {
        if (typeParametersLength != 0 || !Collection.class.isAssignableFrom(raw)) {
            return false;
        }

        Class<?> tempClass = raw;
        while (Object.class != tempClass) {
            Type genericSuperclass = tempClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0 && type.equals(actualTypeArguments[0].getTypeName())) {
                    return false;
                }
            }
            tempClass = tempClass.getSuperclass();
        }
        return true;
    }

    private static Type forNameWithOneGenericType(Class<?> rawClass, String genericType, String typeName) {
        final Type[] args = new Type[]{forName(genericType)};
        final ParameterizedTypeImpl parameterizedType = ParameterizedTypeImpl.make(rawClass, args, null);
        TYPE_NAME_CACHE.put(typeName, parameterizedType);
        return parameterizedType;
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
                field = getGenericFieldFromClass(rawClass, typeName);
                if (field == null) {
                    return builder.toString();
                }
                field.setAccessible(true);
                GENERIC_FIELD_CACHE.put(cacheKey, field);
            }

            String genericType = invokeGetFieldType(field, result);
            // only collection field need to filter raw generic type
            if (isCollection(field.getType().getName())) {
                genericType = filterRawGenericType(genericType);
            }
            genericType = StringUtil.isEmpty(genericType) ? DEFAULT_CLASS_NAME : genericType;
            builder.append(genericType);
            if (i == typeParameters.length - 1) {
               return builder.toString();
            }
            builder.append(COMMA);
        }
        return builder.toString();
    }

    private static Field getGenericFieldFromClass(Class<?> rawClass, String typeName) {
        if (rawClass == null) {
            return null;
        }
        for (Field declaredField : rawClass.getDeclaredFields()) {
            final String fieldGenericType = declaredField.getGenericType().getTypeName();
            // equals T
            if (fieldGenericType.equals(typeName)) {
                return declaredField;
            }
            // java.util.List<T> contains T && field is collection
            if (fieldGenericType.contains(typeName) && isCollection(declaredField.getType().getName())) {
                return declaredField;
            }
        }
        // search super class
        return getGenericFieldFromClass(rawClass.getSuperclass(), typeName);
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
     * Converts a collection to a string representation.
     * <p>
     * eg: {@code List<String>} to java.util.ArrayList-java.lang.String
     * <p>
     * {@code List<List<String>>} to java.util.ArrayList-java.util.ArrayList,java.lang.String,java.lang.Integer
     */
    private static String collectionToString(Collection<?> result) {
        StringBuilder builder = new StringBuilder();
        builder.append(result.getClass().getName());

        if (result.isEmpty()) {
            return builder.toString();
        }

        builder.append(HORIZONTAL_LINE);

        List<String> linkedList = new LinkedList<>();
        boolean appendInnerCollection = true;
        for (Object innerObj : result) {
            if (innerObj == null) {
                continue;
            }

            Collection<?> innerCollection = null;
            if (innerObj instanceof Collection<?>) {
                innerCollection = (Collection<?>) innerObj;
            }
            if (innerCollection == null) {
                builder.append(getName(innerObj));
                return builder.toString();
            }

            if (appendInnerCollection) {
                linkedList.add(innerObj.getClass().getName());
                appendInnerCollection = false;
            }

            for (Object innerElement : innerCollection) {
                if (innerElement == null) {
                    continue;
                }

                // By default, the data types in list<list<Object,Object>> are the same,
                // and the inner list gets the first non-null element, which is break.
                linkedList.add(innerElement.getClass().getName());
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
        if (result.isEmpty()) {
            return result.getClass().getName();
        }

        String resultClassName = result.getClass().getName();
        final TypeVariable<?>[] typeParameters = result.getClass().getTypeParameters();
        if (ArrayUtils.isEmpty(typeParameters)) {
            return resultClassName;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(resultClassName).append(HORIZONTAL_LINE);

        // only get the first not empty element
        for (Map.Entry<?, ?> entry : result.entrySet()) {
            final Object value = entry.getValue();
            if (isEmpty(value)) {
                continue;
            }
            String valueClassName = getName(value);

            if (typeParameters.length == 1) {
                builder.append(valueClassName);
            } else {
                String keyClassName = entry.getKey() == null ? DEFAULT_CLASS_NAME : entry.getKey().getClass().getName();
                builder.append(keyClassName).append(COMMA).append(valueClassName);
            }
            break;
        }

        return builder.toString();
    }

    private static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Collection<?>) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map<?, ?>) {
            return ((Map<?, ?>) value).isEmpty();
        }
        return false;
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

    public static String arrayObjectToString(Object[] objects) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < objects.length; i++) {
            builder.append(objects[i].getClass().getName());
            if (i != objects.length - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    public static String errorSerializeToString(Object object) {
        if (object instanceof Object[]) {
            return arrayObjectToString((Object[]) object);
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
        StringBuilder builder = null;
        String[] types = StringUtil.split(genericType, HORIZONTAL_LINE);
        for (String type : types) {
            if (StringUtil.isNullWord(type) || isCollection(type)) {
                continue;
            }
            if (builder == null) {
                builder = new StringBuilder();
            }
            builder.append(HORIZONTAL_LINE).append(type);
        }
        return builder == null ? StringUtil.EMPTY : builder.substring(1);
    }

    public static boolean isCollection(String genericType) {
        if (StringUtil.isEmpty(genericType)) {
            return false;
        }
        switch (genericType) {
            case "java.util.List":
            case "java.util.Set":
            case "java.util.ArrayList":
            case "java.util.LinkedList":
            case "java.util.HashSet":
            case "java.util.LinkedHashSet":
            case "java.util.TreeSet":
            case "java.util.Collections$EmptyList":
            case "java.util.Collections$EmptySet":
                return true;
            default:
                try {
                    Class<?> clazz = Class.forName(genericType);
                    return Collection.class.isAssignableFrom(clazz);
                } catch (ClassNotFoundException e) {
                    return false;
                }
        }
    }

    public static Collection<Collection<?>> toNestedCollection(Object object) {
        Collection<Collection<?>> collection = null;
        if (object instanceof Collection<?>) {
            collection = (Collection<Collection<?>>) object;
        }

        if (CollectionUtil.isEmpty(collection)) {
            return null;
        }

        for (Object innerCollection : collection) {
            if (innerCollection == null) {
                continue;
            }
            if (!(innerCollection instanceof Collection<?>)) {
                return null;
            }
        }

        return collection;
    }

    public static boolean isProtobufClass(Class<?> clazz) {
        if (clazz == null || clazz.isInterface()) {
            return false;
        }
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass == null) {
            return false;
        }
        return StringUtil.startWith(superclass.getName(), PROTOCOL_BUFFER_PACKAGE);
    }

    public static boolean isJodaLocalDateTime(String typeName) {
        return JODA_LOCAL_DATE_TIME.equals(typeName);
    }

    public static boolean isJodaLocalDate(String typeName) {
        return JODA_LOCAL_DATE.equals(typeName);
    }

    public static boolean isJodaLocalTime(String typeName) {
        return JODA_LOCAL_TIME.equals(typeName);
    }

    public static boolean isJodaDateTime(String typeName) {
        return JODA_DATE_TIME.equals(typeName);
    }

    public static boolean isJoinPoint(String typeName) {
        return METHOD_INVOCATION_PROCEEDING_JOIN_POINT.equals(typeName);
    }

}
