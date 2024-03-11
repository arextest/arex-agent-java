package io.arex.inst.cache;

import com.google.auto.service.AutoService;
import io.arex.inst.cache.arex.ArexMockInstrumentation;
import io.arex.inst.cache.caffeine.CaffeineAsyncInstrumentation;
import io.arex.inst.cache.caffeine.CaffeineSyncInstrumentation;
import io.arex.inst.cache.guava.GuavaCacheInstrumentation;
import io.arex.inst.cache.spring.SpringCacheInstrumentation;
import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

@AutoService(ModuleInstrumentation.class)
public class CacheModuleInstrumentation extends ModuleInstrumentation {
    public CacheModuleInstrumentation() {
        super("arex-cache");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(
                new SpringCacheInstrumentation(),
                new ArexMockInstrumentation(),
                new GuavaCacheInstrumentation(),
                new CaffeineSyncInstrumentation(),
                new CaffeineAsyncInstrumentation());
    }
}
