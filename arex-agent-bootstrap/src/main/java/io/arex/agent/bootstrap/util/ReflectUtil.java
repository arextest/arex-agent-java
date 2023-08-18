package io.arex.agent.bootstrap.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeSet;

public class ReflectUtil {

    public static Object getFieldOrInvokeMethod(Reflector<Object> reflector, Object obj) throws Exception {
        Object result = null;
        Object instance = reflector.reflect();
        if (instance instanceof Field) {
            Field field = (Field) instance;
            boolean accessible = field.isAccessible();
            if (!accessible) {
                field.setAccessible(true);
            }
            result = field.get(obj);
            field.setAccessible(accessible);
        }
        if (instance instanceof Method) {
            Method method = (Method) instance;
            boolean accessible = method.isAccessible();
            if (!accessible) {
                method.setAccessible(true);
            }
            result = method.invoke(obj);
            method.setAccessible(accessible);
        }
        return result;
    }

    public interface Reflector<T> {
        T reflect() throws Exception;
    }

    /**
     * Return new instance of collection by type name per time, do not cache.
     * @param typeName type name of collection
     * @return new instance of collection
     */
    public static <T> Collection<T> getCollectionInstance(String typeName) {
        if ("java.util.ArrayList".equals(typeName)) {
            return new ArrayList<>();
        }
        if ("java.util.LinkedList".equals(typeName)) {
            return new LinkedList<>();
        }
        if ("java.util.Collections$EmptyList".equals(typeName)) {
            return Collections.emptyList();
        }
        if ("java.util.HashSet".equals(typeName)) {
            return new HashSet<>();
        }
        if ("java.util.LinkedHashSet".equals(typeName)) {
            return new LinkedHashSet<>();
        }
        if ("java.util.TreeSet".equals(typeName)) {
            return new TreeSet<>();
        }
        if ("java.util.Collections$EmptySet".equals(typeName)) {
            return Collections.emptySet();
        }

        try {
            return (Collection<T>) Class.forName(typeName).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
