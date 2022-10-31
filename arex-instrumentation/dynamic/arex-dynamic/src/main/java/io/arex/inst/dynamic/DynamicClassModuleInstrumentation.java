package io.arex.inst.dynamic;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.model.DynamicClassEntity;
import io.arex.foundation.util.CollectionUtil;

import java.util.ArrayList;
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
        List<DynamicClassEntity> dynamicClassList = ConfigManager.INSTANCE.getDynamicClassList();
        if (CollectionUtil.isNotEmpty(dynamicClassList)) {
            Map<String, List<DynamicClassEntity>> dynamicMap = dynamicClassList.stream().collect(Collectors.groupingBy(
                    DynamicClassEntity::getClazzName));
            for (Map.Entry<String, List<DynamicClassEntity>> entry : dynamicMap.entrySet()) {
                typeInstList.add(new DynamicClassInstrumentation(entry.getValue()));
            }
        }
        return typeInstList;
    }
}
