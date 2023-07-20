package io.arex.agent.bootstrap.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
}
