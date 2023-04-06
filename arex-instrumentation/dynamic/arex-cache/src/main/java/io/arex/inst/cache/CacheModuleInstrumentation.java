package io.arex.inst.cache;

import com.google.auto.service.AutoService;

import io.arex.inst.cache.arex.ArexMockInstrumentation;
import io.arex.inst.cache.spring.SpringCacheInstrumentation;
import java.util.Arrays;
import java.util.List;

import io.arex.inst.extension.ModuleInstrumentation;
import io.arex.inst.extension.TypeInstrumentation;

@AutoService(ModuleInstrumentation.class)
public class CacheModuleInstrumentation extends ModuleInstrumentation {
    public CacheModuleInstrumentation() {
        super("arex-cache");
    }

    @Override
    public List<TypeInstrumentation> instrumentationTypes() {
        return Arrays.asList(new SpringCacheInstrumentation(), new ArexMockInstrumentation());
    }
}
