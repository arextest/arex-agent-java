package io.arex.inst.dubbo.apache.v3;

import com.google.auto.service.AutoService;
import io.arex.inst.dubbo.apache.v3.stream.DubboStreamProviderInstrumentation;
import io.arex.inst.dubbo.apache.v3.stream.DubboStreamConsumerInstrumentation;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.model.ComparableVersion;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * DubboModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class DubboModuleInstrumentation extends ModuleInstrumentation {

    public DubboModuleInstrumentation() {
        super("dubbo-apache-v3", ModuleDescription.builder()
                .name("dubbo").supportFrom(ComparableVersion.of("3.0")).build());
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
