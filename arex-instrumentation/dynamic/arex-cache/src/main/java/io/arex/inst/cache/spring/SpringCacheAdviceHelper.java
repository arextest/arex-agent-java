package io.arex.inst.cache.spring;

import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.DynamicClassEntity;

import java.lang.reflect.Method;

public class SpringCacheAdviceHelper {
    public static boolean needRecordOrReplay(Method method) {
        if (!ContextManager.needRecordOrReplay() || method == null) {
            return false;
        }

        String methodSignature = buildMethodSignature(method);

        DynamicClassEntity dynamicEntity = Config.get().getDynamicEntity(methodSignature);

        return dynamicEntity != null;
    }

    private static String buildMethodSignature(Method method) {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return className + methodName;
        }
        return className + methodName + parameterTypes.length;
    }
}
