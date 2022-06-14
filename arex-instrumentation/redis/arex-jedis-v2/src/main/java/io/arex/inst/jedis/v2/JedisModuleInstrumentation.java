package io.arex.inst.jedis.v2;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class JedisModuleInstrumentation extends ModuleInstrumentation {
    public JedisModuleInstrumentation() {
        super("jedis-v2", ModuleDescription.builder()
                .addPackage("Jedis", "2")
                .build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new JedisFactoryInstrumentation(target));
    }
}
