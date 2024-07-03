package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.serializer.Serializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

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
        ArexMocker recordMocker1 = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        recordMocker1.setOperationName(ArexConstants.MERGE_RECORD_NAME);
        recordMocker1.setRecordId("mock");
        recordMocker1.setReplayId("mock");
        recordMocker1.setTargetRequest(new Mocker.Target());
        recordMocker1.setTargetResponse(new Mocker.Target());
        recordMocker1.getTargetResponse().setBody("mock");
        recordMocker1.setCreationTime(System.currentTimeMillis());
        ArexMocker recordMocker2 = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        recordMocker2.setOperationName(ArexConstants.MERGE_RECORD_NAME);
        recordMocker2.setRecordId("mock");
        recordMocker2.setReplayId("mock");
        recordMocker2.setTargetRequest(new Mocker.Target());
        recordMocker2.setTargetResponse(new Mocker.Target());
        recordMocker2.getTargetResponse().setBody("mock");
        recordMocker2.setCreationTime(System.currentTimeMillis() + 1);

        MergeDTO mergeDTO = new MergeDTO();

        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            ArexContext context = Mockito.mock(ArexContext.class);
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
            Map<Integer, List<Mocker>> cachedReplayResultMap = new HashMap<>();
            Mockito.when(context.getCachedReplayResultMap()).thenReturn(cachedReplayResultMap);
            Mockito.when(MockUtils.queryMockers(any())).thenReturn(CollectionUtil.newArrayList(recordMocker1, recordMocker2));
            Mockito.when(Serializer.deserialize("mock", ArexConstants.MERGE_TYPE))
                    .thenReturn(CollectionUtil.newArrayList(mergeDTO));
        };
        Runnable mocker2 = () -> {
            Mockito.when(MockUtils.checkResponseMocker(any())).thenReturn(true);
            requestMocker.getTargetResponse().setBody("mock");
            Mockito.when(MockUtils.executeReplay(any(), any())).thenReturn(requestMocker);

            mergeDTO.setCategory(MockCategoryType.DYNAMIC_CLASS.getName());
        };
        return Stream.of(
                arguments(emptyMocker),
                arguments(mocker1),
                arguments(mocker2)
        );
    }
}