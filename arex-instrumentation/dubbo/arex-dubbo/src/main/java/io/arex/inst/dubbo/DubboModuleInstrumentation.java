package io.arex.inst.dubbo;

import com.google.auto.service.AutoService;
import io.arex.inst.dubbo.stream.DubboStreamConsumerInstrumentation;
import io.arex.inst.dubbo.stream.DubboStreamProviderInstrumentation;
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
        super("dubbo");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(
                new DubboConsumerInstrumentation(),
                new DubboProviderInstrumentation(),
                new DubboStreamConsumerInstrumentation(),
                new DubboStreamProviderInstrumentation());
    }
}
