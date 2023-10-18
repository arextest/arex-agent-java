package io.arex.inst.dynamic;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.model.DynamicClassEntity;

import io.arex.inst.runtime.model.DynamicClassStatusEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DynamicClassModuleInstrumentation
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
        return buildTypeInstrumentation(Config.get().getDynamicClassList());
    }

    private List<TypeInstrumentation> buildTypeInstrumentation(List<DynamicClassEntity> dynamicClassList) {
        List<TypeInstrumentation> typeInstList = new ArrayList<>();
        if (CollectionUtil.isEmpty(dynamicClassList)) {
            return typeInstList;
        }

        Map<String, List<DynamicClassEntity>> dynamicMap = dynamicClassList.stream()
            .filter(item -> item.getStatus() == DynamicClassStatusEnum.RETRANSFORM)
            .collect(Collectors.groupingBy(DynamicClassEntity::getClazzName));

        for (Map.Entry<String, List<DynamicClassEntity>> entry : dynamicMap.entrySet()) {
            typeInstList.add(new DynamicClassInstrumentation(entry.getValue()));
        }

        return typeInstList;
    }
}
