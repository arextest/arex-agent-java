package io.arex.inst.spring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.util.MockUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class SpringCacheInstrumentationTest {
    private static SpringCacheInstrumentation target;

    @BeforeAll
    static void setUp() {
        target = new SpringCacheInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(SpringCacheAdviceHelper.class);
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        ElementMatcher<TypeDescription> matcher = target.typeMatcher();
        assertTrue(matcher.toString().contains("org.springframework.cache.interceptor.CacheAspectSupport"));
    }

    @Test
    void methodAdvice() {
        List<MethodInstrumentation> methodInstrumentations = target.methodAdvices();
        assertTrue(methodInstrumentations.get(0).getMethodMatcher().toString().contains("execute"));
    }

    @Test
    void onEnterRecord() {
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(SpringCacheAdviceHelper.needRecordOrReplay(any())).thenReturn(true);
        boolean onEnter = SpringCacheInstrumentation.SpringCacheAdvice.onEnter(null, null, null, null);
        assertFalse(onEnter);
    }

    @Test
    void onEnterReplay() {
        AtomicReference<DynamicClassExtractor> atomicReference = new AtomicReference();
        try(MockedConstruction mocked = Mockito.mockConstruction(DynamicClassExtractor.class, ((mock, context) -> atomicReference.set(mock)))) {
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            DynamicClassExtractor extractor = new DynamicClassExtractor("testClassName", "testOperation", null, null);
            Mockito.when(SpringCacheAdviceHelper.needRecordOrReplay(any())).thenReturn(true);
            Mockito.when(SpringCacheAdviceHelper.createDynamicExtractor(any(), any())).thenReturn(atomicReference.get());
            Mockito.when(atomicReference.get().replay()).thenReturn(MockResult.success("test"));
            boolean onEnter = SpringCacheInstrumentation.SpringCacheAdvice.onEnter(null, null, extractor, null);
            assertTrue(onEnter);
        }
    }

    @Test
    void onExitRecord() {
        AtomicReference<DynamicClassExtractor> atomicReference = new AtomicReference();
        try(MockedConstruction mocked = Mockito.mockConstruction(DynamicClassExtractor.class, ((mock, context) -> atomicReference.set(mock)))) {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
            DynamicClassExtractor extractor = new DynamicClassExtractor("testClassName", "testOperation", null, null);
            Mockito.when(atomicReference.get().replay()).thenReturn(MockResult.success("test"));
            String result = "test";
            SpringCacheInstrumentation.SpringCacheAdvice.onExit(extractor, null, null, result);
            Mockito.verify(atomicReference.get(), Mockito.times(1)).setResponse(result);
        }
    }

    @Test
    void onExitReplay() {
        MockResult success = MockResult.success("success");
        MockResult throwable = MockResult.success(new NullPointerException());
        String testResult = "";
        assertDoesNotThrow(() -> SpringCacheInstrumentation.SpringCacheAdvice.onExit(null, success, null, testResult));
        assertDoesNotThrow(() -> SpringCacheInstrumentation.SpringCacheAdvice.onExit(null, throwable, null, null));

    }
}