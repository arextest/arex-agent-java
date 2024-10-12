package io.arex.inst.spring;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.inst.runtime.config.Config;

import java.util.Collections;
import java.util.List;

@AutoService(ModuleInstrumentation.class)
public class SpringModuleInstrumentation extends ModuleInstrumentation {

    public SpringModuleInstrumentation() {
        super("spring-scan");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        // spring-scan module is special, it will be disabled if coveragePackages is not empty
        if (CollectionUtil.isNotEmpty(Config.get().getCoveragePackages())) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SpringComponentScanInstrumentation());
    }
}
