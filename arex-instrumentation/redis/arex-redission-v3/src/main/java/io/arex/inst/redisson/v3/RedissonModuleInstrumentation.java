package io.arex.inst.redisson.v3;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.model.ComparableVersion;

import java.util.Arrays;
import java.util.List;

/**
 * RedissonModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class RedissonModuleInstrumentation extends ModuleInstrumentation {
    public RedissonModuleInstrumentation() {
        super("redisson-v3", ModuleDescription.builder()
                .name("Redisson").supportFrom(ComparableVersion.of("3.0")).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new RedissonInstrumentation());
    }
}
