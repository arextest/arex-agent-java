package io.arex.inst.config.apollo;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * DubboModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class ApolloModuleInstrumentation extends ModuleInstrumentation {

    public ApolloModuleInstrumentation() {
        super("apollo-config");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(
                new ApolloRemoteConfigRepositoryInstrumentation(),
                new ApolloDefaultConfigInstrumentation(),
                new ApolloLocalFileConfigRepositoryInstrumentation());
    }
}
