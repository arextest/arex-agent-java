package io.arex.inst.runtime.context;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.cache.LoadedModuleCache;
import org.junit.jupiter.api.Test;

class ResourceManagerTest {

    @Test
    void registerResources() {
        ResourceManager.registerResources(Thread.currentThread().getContextClassLoader());
        ResourceManager.registerResources(Thread.currentThread().getContextClassLoader());

        assertNotNull(LoadedModuleCache.get("Byte Buddy agent"));
    }
}