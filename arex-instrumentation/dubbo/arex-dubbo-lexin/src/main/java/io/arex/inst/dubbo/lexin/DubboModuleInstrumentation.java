package io.arex.inst.dubbo.lexin;

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
        super("dubbo-lexin", ModuleDescription.builder()
                .name("dubbo").supportFrom(ComparableVersion.of("2.17")).supportTo(ComparableVersion.of("2.99")).build());
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
