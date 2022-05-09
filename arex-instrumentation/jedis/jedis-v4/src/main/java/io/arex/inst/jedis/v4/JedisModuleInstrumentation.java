package io.arex.inst.jedis.v4;

import com.google.auto.service.AutoService;
import io.arex.api.instrumentation.ModuleDescription;
import io.arex.api.instrumentation.ModuleInstrumentation;
import io.arex.api.instrumentation.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class JedisModuleInstrumentation extends ModuleInstrumentation {
    public JedisModuleInstrumentation() {
        super("jedis-v4", ModuleDescription.builder()
                .addPackage("Jedis", "4")
                .build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new JedisFactoryInstrumentation(target));
    }
}
