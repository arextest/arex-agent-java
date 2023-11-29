package io.arex.inst.cache.spring;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.cache.TestArexMock;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.util.MockUtils;

import java.lang.reflect.Method;
import java.util.List;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class SpringCacheInstrumentationTest {
    private static SpringCacheInstrumentation target;

    @BeforeAll
    static void setUp() {
        target = new SpringCacheInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
        Mockito.mockStatic(SpringCacheAdviceHelper.class);
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
    void onEnter() throws NoSuchMethodException {
        Method test1 = TestArexMock.class.getDeclaredMethod("testWithCacheableAnnotation", String.class, int.class);
        try(MockedConstruction ignored = Mockito.mockConstruction(DynamicClassExtractor.class, ((extractor, context) -> {
            Mockito.when(extractor.replay()).thenReturn(MockResult.success("test"));
        }))) {
            Method testReturnVoid = TestArexMock.class.getDeclaredMethod("testReturnVoid");
            boolean actualResult = SpringCacheInstrumentation.SpringCacheAdvice.onEnter(testReturnVoid, null, null, null);
            assertFalse(actualResult);

            // not record
            Mockito.when(SpringCacheAdviceHelper.needRecordOrReplay(any())).thenReturn(false);
            actualResult = SpringCacheInstrumentation.SpringCacheAdvice.onEnter(test1, new Object[]{ "name", 18 }, null, null);
            assertFalse(actualResult);

            // record
            Mockito.when(SpringCacheAdviceHelper.needRecordOrReplay(any())).thenReturn(true);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            DynamicClassExtractor extractor = new DynamicClassExtractor(test1, new Object[]{"mock"}, "#val", null);
            actualResult = SpringCacheInstrumentation.SpringCacheAdvice.onEnter(test1, new Object[]{ "name", 18 }, extractor, null);
            assertFalse(actualResult);

            // replay
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            actualResult = SpringCacheInstrumentation.SpringCacheAdvice.onEnter(test1, new Object[]{ "name", 18 }, extractor, null);
            assertTrue(actualResult);
        }
    }

    @Test
    void onExit() throws NoSuchMethodException {
        Method testReturnVoid = TestArexMock.class.getDeclaredMethod("testReturnVoid");
        SpringCacheInstrumentation.SpringCacheAdvice.onExit(testReturnVoid, null, null, null, null);

        Method test1 = TestArexMock.class.getDeclaredMethod("testWithCacheableAnnotation", String.class, int.class);
        // replay with success result
        MockResult success = MockResult.success("success");
        SpringCacheInstrumentation.SpringCacheAdvice.onExit(test1, null, success, null, null);
        // replay with throwable
        MockResult throwable = MockResult.success(new NullPointerException());
        SpringCacheInstrumentation.SpringCacheAdvice.onExit(test1, null, throwable, null, null);

        // record
        try(MockedConstruction ignored = Mockito.mockConstruction(DynamicClassExtractor.class, ((extractor, context) -> {
            Mockito.doReturn(true).when(extractor).recordResponse(throwable);
        }))) {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
            DynamicClassExtractor extractor = new DynamicClassExtractor(test1, new Object[]{"mock"}, "#val", null);
            SpringCacheInstrumentation.SpringCacheAdvice.onExit(test1, extractor, null, null, throwable);
            Mockito.verify(extractor, Mockito.times(1)).recordResponse(throwable);
        }
    }
}