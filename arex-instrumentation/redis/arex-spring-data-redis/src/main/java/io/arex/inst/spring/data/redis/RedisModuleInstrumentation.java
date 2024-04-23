package io.arex.inst.spring.data.redis;

import static java.util.Arrays.asList;
import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.util.List;

/**
 * RedisModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class RedisModuleInstrumentation extends ModuleInstrumentation {
    public RedisModuleInstrumentation() {
        super("spring-data-redis");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return asList(
            new RedisTemplateInstrumentation(),
            new OperationsInstrumentation());
    }
}
