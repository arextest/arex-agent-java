package io.arex.inst.lettuce.v6;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

/**
 * LettuceModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class LettuceModuleInstrumentation extends ModuleInstrumentation {
    public LettuceModuleInstrumentation() {
        super("lettuce-v6", ModuleDescription.builder()
                .name("lettuce.core").supportFrom(5, 0).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new StatefulRedisConnectionImplInstrumentation(), new RedisClientInstrumentation());
    }
}
