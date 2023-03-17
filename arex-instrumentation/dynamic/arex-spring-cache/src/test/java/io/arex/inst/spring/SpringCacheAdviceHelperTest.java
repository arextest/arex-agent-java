package io.arex.inst.spring;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.dynamic.common.DynamicClassExtractor;
import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.DynamicClassEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class SpringCacheAdviceHelperTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void testNeedRecordOrReplay() throws NoSuchMethodException {
        Method method = this.getClass().getDeclaredMethod("testMethod", String.class);
        Method method2 = this.getClass().getDeclaredMethod("testMethod2", String.class);
        Method method3 = SpringCacheModuleInstrumentationTest.class.getDeclaredMethod("testInstrumentationTypes");

        SpringCacheAdviceHelper.buildMap();
        assertFalse(SpringCacheAdviceHelper.needRecordOrReplay(method));

        ConfigBuilder configBuilder = ConfigBuilder.create("test");
        configBuilder.build();
        SpringCacheAdviceHelper.buildMap();
        assertFalse(SpringCacheAdviceHelper.needRecordOrReplay(method));


        List<DynamicClassEntity> entities = Arrays.asList(new DynamicClassEntity("io.arex.inst.spring.SpringCacheAdviceHelperTest", "testMethod", null, null));
        configBuilder.dynamicClassList(entities).build();

        SpringCacheAdviceHelper.buildMap();


        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
        assertFalse(SpringCacheAdviceHelper.needRecordOrReplay(method));

        // return false because dynamic != method
        Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        assertFalse(SpringCacheAdviceHelper.needRecordOrReplay(method2));
        assertFalse(SpringCacheAdviceHelper.needRecordOrReplay(method3));

        assertTrue(SpringCacheAdviceHelper.needRecordOrReplay(method));
    }

    @Test
    void testCreatDynamic() {
        AtomicReference<DynamicClassExtractor> atomicReference = new AtomicReference();
        try(MockedConstruction mocked = Mockito.mockConstruction(DynamicClassExtractor.class, ((mock, context) -> atomicReference.set(mock)))) {

            Method method = this.getClass().getDeclaredMethod("testMethod", String.class);
            SpringCacheAdviceHelper.createDynamicExtractor(method, null);
            assertNotNull(atomicReference.get());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    String testMethod(String arg1) {
        return "result";
    }

    String testMethod2(String arg1) {
        return "result";
    }



}