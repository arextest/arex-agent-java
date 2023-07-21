package io.arex.inst.dubbo.common;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.IgnoreUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

class DubboExtractorTest {
    static DubboAdapterTest adapter;

    @BeforeAll
    static void setUp() {
        adapter = new DubboAdapterTest();
        Mockito.mockStatic(IgnoreUtils.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        adapter = null;
        Mockito.clearAllCaches();
    }
    @Test
    void buildMocker() {
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetRequest(new Mocker.Target());
        mocker.setTargetResponse(new Mocker.Target());
        DubboExtractor.buildMocker(mocker, adapter, null, null);
    }

    @ParameterizedTest
    @MethodSource("shouldSkipCase")
    void shouldSkip(Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        boolean result = DubboExtractor.shouldSkip(adapter);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> shouldSkipCase() {
        Runnable mocker1 = () -> {
            adapter.setServiceOperation("org.apache.dubbo.metadata.MetadataService.getMetadataInfo");
        };
        Runnable mocker2 = () -> {
            adapter.setServiceOperation("");
            adapter.setCaseId("mock");
        };
        Runnable mocker3 = () -> {
            adapter.setCaseId("");
            adapter.setForceRecord(true);
        };
        Runnable mocker4 = () -> {
            adapter.setForceRecord(false);
            adapter.setReplayWarmUp(true);
        };
        Runnable mocker5 = () -> {
            adapter.setReplayWarmUp(false);
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(true);
        };
        Runnable mocker6 = () -> {
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(false);
        };
        Predicate<Boolean> predicate1 = result -> result;
        Predicate<Boolean> predicate2 = result -> !result;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate2),
                arguments(mocker3, predicate2),
                arguments(mocker4, predicate1),
                arguments(mocker5, predicate1),
                arguments(mocker6, predicate2)
        );
    }

    @Test
    void setResponseHeader() {
        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock"));
        Map<String, String> map = new HashMap<>();
        DubboExtractor.setResponseHeader(map::put);
        assertTrue(map.containsKey(ArexConstants.RECORD_ID));
        assertTrue(map.containsKey(ArexConstants.REPLAY_ID));
    }
}