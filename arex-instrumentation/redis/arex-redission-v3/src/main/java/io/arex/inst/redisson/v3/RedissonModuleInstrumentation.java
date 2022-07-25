package io.arex.inst.redisson.v3;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

/**
 * RedissonModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class RedissonModuleInstrumentation extends ModuleInstrumentation {
    public RedissonModuleInstrumentation() {
        super("redisson-v3", ModuleDescription.builder()
            .addPackage("Redisson", "3").build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new RedissonInstrumentation(target));
    }
}
