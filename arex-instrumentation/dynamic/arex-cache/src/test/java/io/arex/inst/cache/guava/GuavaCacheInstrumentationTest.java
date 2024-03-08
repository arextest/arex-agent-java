package io.arex.inst.cache.guava;

import io.arex.inst.cache.common.CacheLoaderUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class GuavaCacheInstrumentationTest {
    private static GuavaCacheInstrumentation target;

    @BeforeAll
    static void setUp() {
        target = new GuavaCacheInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
        Mockito.mockStatic(CacheLoaderUtil.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        ElementMatcher<TypeDescription> matcher = target.typeMatcher();
        assertTrue(matcher.toString().contains("com.google.common.cache.LocalCache"));
    }

    @Test
    void methodAdvices() {
        assertEquals(2, target.methodAdvices().size());
        assertTrue(target.methodAdvices().get(0).getMethodMatcher().toString().contains("get"));
    }

    @Test
    void onExit() throws Exception {
    }
}
