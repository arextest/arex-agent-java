package io.arex.inst.jedis.v4;

import com.google.auto.service.AutoService;
import io.arex.foundation.api.ModuleInstrumentation;
import io.arex.foundation.api.ModuleDescription;
import io.arex.foundation.api.TypeInstrumentation;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class JedisModuleInstrumentation extends ModuleInstrumentation {
    public JedisModuleInstrumentation() {
        super("jedis-v4", ModuleDescription.builder()
                .name("Jedis").supportFrom(4, 0).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new JedisFactoryInstrumentation());
    }
}
