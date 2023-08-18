package io.arex.inst.dubbo.alibaba;

import com.google.auto.service.AutoService;
import io.arex.agent.bootstrap.model.ComparableVersion;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * DubboModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class DubboModuleInstrumentation extends ModuleInstrumentation {

    public DubboModuleInstrumentation() {
        super("dubbo-alibaba", ModuleDescription.builder()
                .name("Dubbo").supportFrom(ComparableVersion.of("2.0")).supportTo(ComparableVersion.of("2.6")).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(
                new DubboFilterInstrumentation(),
                new DubboCodecInstrumentation(),
                new DubboConsumerInstrumentation(),
                new DubboProviderInstrumentation());
    }
}
