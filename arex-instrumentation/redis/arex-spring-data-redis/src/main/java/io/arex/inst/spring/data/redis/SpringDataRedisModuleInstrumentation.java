package io.arex.inst.spring.data.redis;

import static java.util.Collections.singletonList;
import com.google.auto.service.AutoService;
import io.arex.inst.extension.ModuleDescription;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;
import java.util.List;

/**
 * SpringDataRedisModuleInstrumentation
 */
@AutoService(ModuleInstrumentation.class)
public class SpringDataRedisModuleInstrumentation extends ModuleInstrumentation {
    public SpringDataRedisModuleInstrumentation() {
        super("spring-data-redis");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return singletonList(new SpringDataRedisInstrumentation());
    }
}
