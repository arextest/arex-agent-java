package io.arex.inst.cache.spring;

import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;

import java.lang.reflect.Method;

public class SpringCacheAdviceHelper {
    public static boolean needRecordOrReplay(Method method) {
        if (!ContextManager.needRecordOrReplay() || method == null) {
            return false;
        }
        String className = method.getDeclaringClass().getName();
        return onlyClassMatch(className, method) || methodSignatureMatch(className, method);
    }

    private static boolean onlyClassMatch(String className, Method method) {
        return Config.get().getDynamicEntity(className) != null &&
                method.getParameterTypes().length > 0 &&
                method.getReturnType() != void.class;
    }

    private static boolean methodSignatureMatch(String className, Method method) {
        String methodSignature = buildMethodSignature(className, method);
        return Config.get().getDynamicEntity(methodSignature) != null;
    }

    private static String buildMethodSignature(String className, Method method) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return className + methodName;
        }
        return className + methodName + parameterTypes.length;
    }
}
