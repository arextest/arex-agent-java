package io.arex.inst.dynamic;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * DynamicClassModuleInstrumentation
 *
 *
 * @date 2022/05/16
 */
@AutoService(ModuleInstrumentation.class)
public class DynamicClassModuleInstrumentation extends ModuleInstrumentation {

    public DynamicClassModuleInstrumentation() {
        super("dynamic-class", null);
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        List<TypeInstrumentation> typeInstList = new ArrayList<>();
        List<String> dynamicClassList = ConfigManager.INSTANCE.getDynamicClassList();
        if (CollectionUtil.isNotEmpty(dynamicClassList)) {
            for (String dynamicClassEntity : dynamicClassList) {
                typeInstList.add(new DynamicClassInstrumentation(dynamicClassEntity));
            }
        }
        return typeInstList;
    }
}
