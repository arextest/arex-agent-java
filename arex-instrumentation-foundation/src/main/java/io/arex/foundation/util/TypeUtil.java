package io.arex.foundation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TypeUtil {
    public static final char COMMA = ',';
    public static final char HORIZONTAL_LINE = '-';
    public static final String HORIZONTAL_LINE_STRING = "-";
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeUtil.class);
    private static final ConcurrentMap<String, Type> TYPE_NAME_CACHE = new ConcurrentHashMap<>();

    public static Type forName(String typeName) {
        if (StringUtil.isEmpty(typeName) || typeName.equals(HORIZONTAL_LINE_STRING)) {
            return null;
        }

        Type type = TYPE_NAME_CACHE.get(typeName);
        if (type != null) {
            return type;
        }

        String[] types = StringUtil.split(typeName, HORIZONTAL_LINE);
        if (types.length == 0) {
            return null;
        }

        try {
            Class<?> raw = Class.forName(types[0], false, Thread.currentThread().getContextClassLoader());
            if (types.length > 1 && StringUtil.isNotEmpty(types[1])) {
                types = StringUtil.split(types[1], COMMA);
                Type[] args = new Type[types.length];
                for (int i = 0; i < types.length; i++) {
                    args[i] = Class.forName(types[i], false, Thread.currentThread().getContextClassLoader());
                }
                ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(raw, args);
                TYPE_NAME_CACHE.put(typeName, parameterizedType);
                return parameterizedType;
            } else {
                TYPE_NAME_CACHE.put(typeName, raw);
                return raw;
            }
        } catch (Exception ex) {
            LOGGER.warn("TypeUtil forName", ex);
            return null;
        }
    }

    /**
     * Support List two levels of nesting {@code List<List<Object>>}
     */
    public static String getName(Object result) {
        if (result == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(result.getClass().getName());

        if (!(result instanceof List)) {
            return builder.toString();
        }

        List rootList = (List) result;
        if (rootList.size() < 1) {
            return builder.toString();
        }

        builder.append(HORIZONTAL_LINE);

        List<String> linkedList = new LinkedList<String>();
        for (Object innerObj : rootList) {
            if (innerObj == null) {
                continue;
            }

            String innerObjClassName = innerObj.getClass().getName();
            if (!linkedList.contains(innerObjClassName)) {
                linkedList.add(innerObjClassName);
            }

            if (innerObj instanceof List) {
                List innerList = (List) innerObj;
                for (Object innerElement : innerList) {
                    if (innerElement == null) {
                        continue;
                    }

                    String innerElementClassName = innerElement.getClass().getName();
                    // The data types in list<list<Object, Object>> are considered to be the same
                    linkedList.add(innerElementClassName);
                    break;
                }
            } else {
                break;
            }
        }

        builder.append(StringUtil.join(linkedList.iterator(), ","));
        return builder.toString();
    }

    /**
     * Get raw class from type
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

    public static class ParameterizedTypeImpl implements ParameterizedType {
        private final Class raw;
        private final Type[] args;
        public ParameterizedTypeImpl(Class raw, Type[] args) {
            this.raw = raw;
            this.args = args != null ? args : new Type[0];
        }
        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }
        @Override
        public Type getRawType() {
            return raw;
        }
        @Override
        public Type getOwnerType() {return null;}
    }
}
