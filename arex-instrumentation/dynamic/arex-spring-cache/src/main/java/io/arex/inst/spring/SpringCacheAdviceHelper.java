package io.arex.inst.spring;

import io.arex.agent.bootstrap.util.CollectionUtil;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.DynamicClassEntity;

public class SpringCacheAdviceHelper {
    private static final Map<String, String> DYNAMIC_CLASS_ENTITY = new HashMap<>();

    static {
        buildMap();
    }
    protected static void buildMap() {
        Config config = Config.get();
        if (config == null) {
            return;
        }

        List<DynamicClassEntity> entities = config.dynamicClassEntities();
        if (CollectionUtil.isEmpty(entities)) {
            return;
        }

        for (DynamicClassEntity entity : entities) {
            DYNAMIC_CLASS_ENTITY.put(entity.getClazzName(), entity.getOperation());
        }
    }

    public static boolean needRecordOrReplay(Method method) {
        if (!ContextManager.needRecordOrReplay()) {
            return false;
        }

        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();

        if (StringUtil.isEmpty(className) || StringUtil.isEmpty(methodName)) {
            return false;
        }

        return methodName.equals(DYNAMIC_CLASS_ENTITY.get(className));
    }

    public static DynamicClassExtractor createDynamicExtractor(Method method, Object[] args) {
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String returnType = method.getReturnType().getName();
        return new DynamicClassExtractor(className, methodName, args, returnType);
    }
}
