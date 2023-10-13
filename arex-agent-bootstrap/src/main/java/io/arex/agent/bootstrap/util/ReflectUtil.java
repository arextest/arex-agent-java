package io.arex.agent.bootstrap.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectUtil {

    public static Object getFieldOrInvokeMethod(Reflector<Object> reflector, Object obj, Object... args) throws Exception {
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
            result = method.invoke(obj, args);
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

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... argTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, argTypes);
        } catch (Exception e) {
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?> argType, int argCount) {
        try {
            Class<?>[] argTypes = new Class[argCount];
            Arrays.fill(argTypes, argType);
            return getMethod(clazz, methodName, argTypes);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... argTypes) {
        try {
            return clazz.getDeclaredConstructor(argTypes);
        } catch (Exception e) {
            return null;
        }
    }
}
