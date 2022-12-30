package io.arex.inst.dynamic;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.model.DynamicClassEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DynamicClassModuleInstrumentation
 *
 *
 * @date 2022/05/16
 */
@AutoService(ModuleInstrumentation.class)
public class DynamicClassModuleInstrumentation extends ModuleInstrumentation {

    public DynamicClassModuleInstrumentation() {
        super("dynamic-class");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        List<TypeInstrumentation> typeInstList = new ArrayList<>();
        List<DynamicClassEntity> dynamicClassList = Config.get().dynamicClassEntities();

        if (CollectionUtil.isEmpty(dynamicClassList)) {
            return Collections.emptyList();
        }

        Map<String, List<DynamicClassEntity>> dynamicMap = dynamicClassList.stream().collect(
                Collectors.groupingBy(DynamicClassEntity::getClazzName));
        for (Map.Entry<String, List<DynamicClassEntity>> entry : dynamicMap.entrySet()) {
            typeInstList.add(new DynamicClassInstrumentation(entry.getValue()));
        }
        return typeInstList;
    }
}
