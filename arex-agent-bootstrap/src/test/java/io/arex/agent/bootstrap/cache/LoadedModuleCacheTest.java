package io.arex.agent.bootstrap.cache;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.internal.Pair;
import org.junit.jupiter.api.Test;

class LoadedModuleCacheTest {

    @Test
    void registerProjectModule() {
        LoadedModuleCache.registerProjectModule("test-loaded-module-cache", null);
        LoadedModuleCache.registerProjectModule("test-loaded-module-cache", Pair.of(0, 1));
        assertEquals(Pair.of(0, 1), LoadedModuleCache.get("test-loaded-module-cache"));
    }

    @Test
    void exist() {
        assertFalse(LoadedModuleCache.exist("mock"));
    }
}