package io.arex.inst.jedis.v2;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.model.ComparableVersion;

import java.util.List;

import static java.util.Collections.singletonList;

@AutoService(ModuleInstrumentation.class)
public class JedisModuleInstrumentation extends ModuleInstrumentation {
    public JedisModuleInstrumentation() {
        super("jedis-v2", ModuleDescription.builder()
                .name("Jedis").supportFrom(ComparableVersion.of("2.0")).supportTo(ComparableVersion.of("3.99")).build());
        // todo: check this version
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new JedisFactoryInstrumentation());
    }
}
