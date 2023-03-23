package io.arex.inst.spring;

import com.google.auto.service.AutoService;

import java.util.Collections;
import java.util.List;

import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

@AutoService(ModuleInstrumentation.class)
public class SpringCacheModuleInstrumentation extends ModuleInstrumentation {
    public SpringCacheModuleInstrumentation() {
        super("spring-cache");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Collections.singletonList(new SpringCacheInstrumentation());
    }
}
