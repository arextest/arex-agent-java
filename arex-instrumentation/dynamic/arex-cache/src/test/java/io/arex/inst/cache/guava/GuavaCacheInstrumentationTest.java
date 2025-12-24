package io.arex.inst.cache.guava;

import com.google.common.cache.CacheLoader;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.cache.util.CacheLoaderUtil;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.util.MockUtils;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

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
    void onEnter() throws Exception {
        Object key = "test";
        CacheLoader loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return "test";
            }
        };
        Mockito.when(CacheLoaderUtil.needRecordOrReplay(loader)).thenReturn(true);
        assertFalse(GuavaCacheInstrumentation.GetAdvice.onEnter("get", key, StringUtil.EMPTY, loader, null));

        // record
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        assertFalse(GuavaCacheInstrumentation.GetAdvice.onEnter("get", key, StringUtil.EMPTY, loader, null));

        // replay
        try(MockedConstruction ignored = Mockito.mockConstruction(DynamicClassExtractor.class, ((extractor, context) -> {
            Mockito.when(extractor.replayOrRealCall()).thenReturn(MockResult.success("test"));
        }))) {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            assertTrue(GuavaCacheInstrumentation.GetAdvice.onEnter("get", key, StringUtil.EMPTY, loader, null));
        }
    }

    @Test
    void onExit() throws Exception {
        Object key = "test";
        CacheLoader loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return "test";
            }
        };
        Mockito.when(CacheLoaderUtil.needRecordOrReplay(loader)).thenReturn(true);

        MockedStatic<MockUtils> mockedStatic = Mockito.mockStatic(MockUtils.class);
        Object result = "test";
        Throwable throwable = new RuntimeException("test");
        // not record or replay
        GuavaCacheInstrumentation.GetAdvice.onExit("get", key, StringUtil.EMPTY, loader, null, result, throwable);
        mockedStatic.verify(() -> MockUtils.createDynamicClass(Mockito.anyString(), Mockito.eq("get")), Mockito.never());
        // replay result
        MockResult mockResult = MockResult.success("result");
        GuavaCacheInstrumentation.GetAdvice.onExit("get", key, StringUtil.EMPTY, loader, mockResult, result, throwable);
        mockedStatic.verify(() -> MockUtils.createDynamicClass(Mockito.anyString(), Mockito.eq("get")), Mockito.never());
        // replay throwable
        mockResult = MockResult.success(throwable);
        GuavaCacheInstrumentation.GetAdvice.onExit("get", key, StringUtil.EMPTY, loader, mockResult, result, throwable);
        mockedStatic.verify(() -> MockUtils.createDynamicClass(Mockito.anyString(), Mockito.eq("get")), Mockito.never());

        // record
        AtomicReference<DynamicClassExtractor> atomicReference = new AtomicReference<>();
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(RepeatedCollectManager.exitAndValidate(Mockito.anyString())).thenReturn(true);
        // record
        try(MockedConstruction ignored = Mockito.mockConstruction(DynamicClassExtractor.class, ((extractor, context) -> {
            atomicReference.set(extractor);
        }))) {
            // throwable == null
            GuavaCacheInstrumentation.GetAdvice.onExit("get", key, StringUtil.EMPTY, loader, null, result, null);
            DynamicClassExtractor extractor = atomicReference.get();
            Mockito.verify(extractor, Mockito.times(1)).recordResponse(result);
        }

        // record throwable != null
        AtomicReference<DynamicClassExtractor> atomicReference2 = new AtomicReference<>();
        try(MockedConstruction ignored = Mockito.mockConstruction(DynamicClassExtractor.class, ((extractor, context) -> {
            atomicReference2.set(extractor);
        }))) {
            // throwable != null
            GuavaCacheInstrumentation.GetAdvice.onExit("get", key, StringUtil.EMPTY, loader, null, result, throwable);
            DynamicClassExtractor extractor = atomicReference2.get();
            Mockito.verify(extractor, Mockito.times(1)).recordResponse(throwable);
        }
    }
}
