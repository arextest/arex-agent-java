package io.arex.inst.cache.arex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.cache.TestArexMock;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.extension.MethodInstrumentation;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.util.MockUtils;
import java.lang.reflect.Method;
import java.util.List;
import net.bytebuddy.description.method.MethodDescription.ForLoadedMethod;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class ArexMockInstrumentationTest {
    static ArexMockInstrumentation target = null;

    @BeforeAll
    static void setUp() {
        target = new ArexMockInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
        target = null;
    }

    @Test
    void typeMatcher() {
        assertTrue(target.typeMatcher().matches(TypeDescription.ForLoadedType.of(TestArexMock.class)));
    }

    @Test
    void methodAdvices() throws NoSuchMethodException {
        List<MethodInstrumentation> methodAdvices = target.methodAdvices();

        assertEquals(1, methodAdvices.size());

        Method testWithCacheableAnnotation = TestArexMock.class.getDeclaredMethod("testWithCacheableAnnotation", String.class, int.class);
        assertFalse(methodAdvices.get(0).getMethodMatcher().matches(new ForLoadedMethod(testWithCacheableAnnotation)));

        Method testWithArexMock = TestArexMock.class.getDeclaredMethod("testWithArexMock", String.class, int.class);
        assertTrue(methodAdvices.get(0).getMethodMatcher().matches(new ForLoadedMethod(testWithArexMock)));

        Method testReturnVoid = TestArexMock.class.getDeclaredMethod("testReturnVoid");
        assertFalse(methodAdvices.get(0).getMethodMatcher().matches(new ForLoadedMethod(testReturnVoid)));

        Method testWithoutParameter = TestArexMock.class.getDeclaredMethod("testWithoutParameter");
        assertTrue(methodAdvices.get(0).getMethodMatcher().matches(new ForLoadedMethod(testWithoutParameter)));
    }

    @Test
    void onEnter() throws NoSuchMethodException {
        Method test1 = TestArexMock.class.getDeclaredMethod("testWithCacheableAnnotation", String.class, int.class);
        try(MockedConstruction ignored = Mockito.mockConstruction(DynamicClassExtractor.class, ((extractor, context) -> {
            Mockito.when(extractor.replay()).thenReturn(MockResult.success("test"));
        }))) {
            // record
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            DynamicClassExtractor extractor = new DynamicClassExtractor(test1, new Object[]{"mock"}, "#val", null);
            boolean actualResult = ArexMockInstrumentation.ArexMockAdvice.onEnter(test1, new Object[]{ "name", 18 }, extractor, null);
            assertFalse(actualResult);

            // replay
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            actualResult = ArexMockInstrumentation.ArexMockAdvice.onEnter(test1, new Object[]{ "name", 18 }, extractor, null);
            assertTrue(actualResult);
        }
    }

    @Test
    void onExit() throws NoSuchMethodException {
        // replay with success result
        MockResult success = MockResult.success("success");
        ArexMockInstrumentation.ArexMockAdvice.onExit(null, success, null, null);
        // replay with throwable
        MockResult throwable = MockResult.success(new NullPointerException());
        ArexMockInstrumentation.ArexMockAdvice.onExit(null, throwable, null, null);

        // record
        try(MockedConstruction ignored = Mockito.mockConstruction(DynamicClassExtractor.class, ((extractor, context) -> {
            Mockito.doNothing().when(extractor).recordResponse(throwable);
        }))) {
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
            Method test1 = TestArexMock.class.getDeclaredMethod("testWithCacheableAnnotation", String.class, int.class);
            DynamicClassExtractor extractor = new DynamicClassExtractor(test1, new Object[]{"mock"}, "#val", null);
            ArexMockInstrumentation.ArexMockAdvice.onExit(extractor, null, null, throwable);
            Mockito.verify(extractor, Mockito.times(1)).recordResponse(throwable);
        }
    }
}