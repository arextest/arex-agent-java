package io.arex.inst.lettuce.v6;

import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import io.arex.agent.bootstrap.model.ComparableVersion;
import io.arex.inst.lettuce.v6.cluster.inst.RedisClusterClientInstrumentation;
import io.arex.inst.lettuce.v6.cluster.inst.StatefulRedisClusterConnectionImplInstrumentation;
import io.arex.inst.lettuce.v6.standalone.inst.RedisClientInstrumentation;
import io.arex.inst.lettuce.v6.standalone.inst.StatefulRedisConnectionImplInstrumentation;
import java.util.Arrays;
import java.util.List;

/**
 * LettuceModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class LettuceModuleInstrumentation extends ModuleInstrumentation {
    public LettuceModuleInstrumentation() {
        super("lettuce-v6", ModuleDescription.builder()
                .name("lettuce.core").supportFrom(ComparableVersion.of("6.1")).build());
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(
            new StatefulRedisConnectionImplInstrumentation(),
            new RedisClientInstrumentation(),
            new StatefulRedisClusterConnectionImplInstrumentation(),
            new RedisClusterClientInstrumentation());
    }
}
