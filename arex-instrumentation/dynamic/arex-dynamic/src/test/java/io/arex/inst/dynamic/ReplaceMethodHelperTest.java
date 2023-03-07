package io.arex.inst.dynamic;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.inst.runtime.util.MockUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ReplaceMethodHelperTest {
    static ArexMocker mocker = null;
    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(TimeCache.class);
        mocker = new ArexMocker();
        mocker.setTargetRequest(new Mocker.Target());
        mocker.setTargetResponse(new Mocker.Target());
    }

    @AfterAll
    static void tearDown() {
        mocker = null;
        Mockito.clearAllCaches();
    }

    @Test
    void testUuidReplay() {
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        mocker.getTargetResponse().setBody("7eb4f958-671a-11ed-9022-0242ac120002");
        Mockito.when(MockUtils.replayMocker(any())).thenReturn(mocker);
        Mockito.when(MockUtils.createDynamicClass(any(), any())).thenReturn(mocker);
        UUID uuid = ReplaceMethodHelper.uuid();
        Assertions.assertEquals("7eb4f958-671a-11ed-9022-0242ac120002", uuid.toString());
    }

    @Test
    void testUuidRecord() {
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(MockUtils.createDynamicClass(any(), any())).thenReturn(mocker);
        Assertions.assertDoesNotThrow(ReplaceMethodHelper::uuid);
    }

    @Test
    void testNextReplay() {
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        mocker.getTargetResponse().setBody("2");
        Mockito.when(MockUtils.replayMocker(any())).thenReturn(mocker);
        Mockito.when(MockUtils.createDynamicClass(any(), any())).thenReturn(mocker);
        int nextInt = ReplaceMethodHelper.nextInt(new Random(), 10);
        Assertions.assertEquals(2, nextInt);
    }

    @Test
    void testNextIntRecord() {
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(MockUtils.createDynamicClass(any(), any())).thenReturn(mocker);
        Assertions.assertDoesNotThrow(() -> ReplaceMethodHelper.nextInt(new Random(), 10));
    }

    @Test
    void testSystemReplay() {
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(TimeCache.get()).thenReturn(2L);
        long replayTime = ReplaceMethodHelper.currentTimeMillis();
        Assertions.assertEquals(2, replayTime);
    }

    @Test
    void testSystemRecord() {
        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Assertions.assertDoesNotThrow(ReplaceMethodHelper::currentTimeMillis);
    }
}