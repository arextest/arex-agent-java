package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.sizeof.AgentSizeOf;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class ReplayUtilTest {

    static ArexMocker requestMocker;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
        requestMocker = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        requestMocker.setOperationName("mock");
        requestMocker.setTargetRequest(new Mocker.Target());
        requestMocker.setTargetResponse(new Mocker.Target());
        Mockito.when(MockUtils.create(any(), any())).thenReturn(requestMocker);
        Mockito.mockStatic(Serializer.class);
    }

    @AfterAll
    static void tearDown() {
        requestMocker = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("replayAllMockerCase")
    void replayAllMocker(Runnable mocker) {
        mocker.run();
        assertDoesNotThrow(ReplayUtil::queryMockers);
    }

    static Stream<Arguments> replayAllMockerCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            ArexContext context = Mockito.mock(ArexContext.class);
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
            Map<Integer, List<Mocker>> cachedReplayResultMap = new HashMap<>();
            Mockito.when(context.getCachedReplayResultMap()).thenReturn(cachedReplayResultMap);
        };
        Runnable mocker2 = () -> {
            Mockito.when(MockUtils.checkResponseMocker(any())).thenReturn(true);
            requestMocker.getTargetResponse().setBody("mock");
            Mockito.when(MockUtils.executeReplay(any(), any())).thenReturn(requestMocker);
        };
        Runnable mocker3 = () -> {
            List<MergeDTO> mergeReplayList = new ArrayList<>();
            MergeDTO mergeDTO = new MergeDTO();
            mergeDTO.setMethodRequestTypeHash(1);
            mergeReplayList.add(mergeDTO);
            Mockito.when(Serializer.deserialize(anyString(), anyString())).thenReturn(mergeReplayList);
        };
        return Stream.of(
                arguments(emptyMocker),
                arguments(mocker1),
                arguments(mocker2),
                arguments(mocker3)
        );
    }
}