package io.arex.inst.lettuce.v5;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.model.ComparableVersion;

import java.util.Arrays;
import java.util.List;

/**
 * LettuceModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class LettuceModuleInstrumentation extends ModuleInstrumentation {
    public LettuceModuleInstrumentation() {
        super("lettuce-v5", ModuleDescription.builder()
                .name("lettuce.core").supportFrom(ComparableVersion.of("5.0")).supportTo(ComparableVersion.of("5.9")).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new StatefulRedisConnectionImplInstrumentation(), new RedisClientInstrumentation());
    }
}
