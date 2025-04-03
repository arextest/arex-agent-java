package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.thirdparty.util.CompressUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.model.ReplayCompareResultDTO;
import io.arex.inst.runtime.serializer.Serializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ReplayUtilTest {

    static ArexMocker requestMocker;
    static ArexContext context;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        context = Mockito.mock(ArexContext.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
        Mockito.mockStatic(Config.class);
        Config config = Mockito.mock(Config.class);
        Mockito.when(Config.get()).thenReturn(config);
        Mockito.when(config.isEnableDebug()).thenReturn(true);
        requestMocker = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        requestMocker.setOperationName("mock");
        requestMocker.setTargetRequest(new Mocker.Target());
        requestMocker.setTargetResponse(new Mocker.Target());
        Mockito.when(MockUtils.create(any(), any())).thenReturn(requestMocker);
        Mockito.mockStatic(Serializer.class);
        Mockito.mockStatic(CompressUtil.class);
    }

    @AfterAll
    static void tearDown() {
        requestMocker = null;
        context = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("queryMockersCase")
    void queryMockers(Runnable mocker) {
        mocker.run();
        assertDoesNotThrow(ReplayUtil::queryMockers);
    }

    static Stream<Arguments> queryMockersCase() {
        List<Mocker> recordMockerList = new ArrayList<>();
        recordMockerList.add(requestMocker);

        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Map<Integer, List<Mocker>> cachedReplayResultMap = new HashMap<>();
            Mockito.when(context.getCachedReplayResultMap()).thenReturn(cachedReplayResultMap);
        };
        Runnable mocker2 = () -> {
            Mockito.when(MockUtils.checkResponseMocker(any())).thenReturn(true);
            requestMocker.setRequest("mock");
            requestMocker.setResponse("mock");
            ArexMocker databaseMocker = new ArexMocker(MockCategoryType.DATABASE);
            databaseMocker.setOperationName("databse@table@select@query");
            databaseMocker.setTargetRequest(new Mocker.Target());
            databaseMocker.getTargetRequest().setBody("mock");
            databaseMocker.setTargetResponse(new Mocker.Target());
            recordMockerList.add(databaseMocker);
            ArexMocker databaseMocker2 = new ArexMocker(MockCategoryType.DATABASE);
            databaseMocker2.setOperationName("databse@table@select@query");
            databaseMocker2.setTargetRequest(new Mocker.Target());
            databaseMocker2.getTargetRequest().setBody("mock");
            databaseMocker2.setTargetResponse(new Mocker.Target());
            recordMockerList.add(databaseMocker2);
            ArexMocker databaseMocker3 = new ArexMocker(MockCategoryType.DATABASE);
            databaseMocker3.setOperationName("databse@table@select@query");
            databaseMocker3.setTargetRequest(new Mocker.Target());
            databaseMocker3.getTargetRequest().setBody("mock");
            databaseMocker3.setTargetResponse(new Mocker.Target());
            databaseMocker3.setCreationTime(System.currentTimeMillis() - 1000);
            recordMockerList.add(databaseMocker3);
            Mockito.when(MockUtils.queryMockers(any())).thenReturn(recordMockerList);
            Mockito.when(Serializer.deserialize(any(), eq(ArexConstants.MOCKER_TARGET_TYPE))).thenReturn(new Mocker.Target());
        };
        Runnable mocker3 = () -> {
            requestMocker.setOperationName("arex.mergeRecord");
            Mockito.when(Serializer.deserialize(any(), eq(ArexConstants.MERGE_TYPE))).thenReturn(null);
        };
        List<MergeDTO> mergeReplayList = new ArrayList<>();
        MergeDTO mergeDTO = new MergeDTO();
        mergeReplayList.add(mergeDTO);
        Runnable mocker4 = () -> {
            Mockito.when(Serializer.deserialize(any(), eq(ArexConstants.MERGE_TYPE))).thenReturn(mergeReplayList);
        };
        Runnable mocker5 = () -> {
            mergeDTO.setCategory(MockCategoryType.DYNAMIC_CLASS.getName());
        };
        return Stream.of(
                arguments(emptyMocker),
                arguments(mocker1),
                arguments(mocker2),
                arguments(mocker3),
                arguments(mocker4),
                arguments(mocker5)
        );
    }

    @Test
    void saveReplayCompareResult() {
        assertDoesNotThrow(ReplayUtil::saveReplayCompareResult);
        // replayCompareResultQueue is empty
        Mockito.when(context.isReplay()).thenReturn(true);
        LinkedBlockingQueue<ReplayCompareResultDTO> replayCompareResultQueue = new LinkedBlockingQueue<>();
        Mockito.when(context.getReplayCompareResultQueue()).thenReturn(replayCompareResultQueue);
        assertDoesNotThrow(ReplayUtil::saveReplayCompareResult);
        // saveReplayCompareResult
        replayCompareResultQueue.offer(new ReplayCompareResultDTO());
        Mockito.when(context.getReplayCompareResultQueue()).thenReturn(replayCompareResultQueue);
        Map<Integer, List<Mocker>> cachedReplayResultMap = new HashMap<>();
        cachedReplayResultMap.put(1, Collections.singletonList(requestMocker));
        Mockito.when(context.getCachedReplayResultMap()).thenReturn(cachedReplayResultMap);
        assertDoesNotThrow(ReplayUtil::saveReplayCompareResult);
    }

    @Test
    void saveRemainCompareResult() {
        assertDoesNotThrow(() -> ReplayUtil.saveRemainCompareResult(null));

        Mockito.when(context.getReplayCompareResultQueue()).thenReturn(new LinkedBlockingQueue<>());
        Map<Integer, List<Mocker>> cachedReplayResultMap = new HashMap<>();
        ArexMocker databaseMocker = new ArexMocker(MockCategoryType.DATABASE);
        databaseMocker.setOperationName("databse@table@select@query");
        databaseMocker.setTargetRequest(new Mocker.Target());
        databaseMocker.getTargetRequest().setBody("mock");
        databaseMocker.setTargetResponse(new Mocker.Target());
        cachedReplayResultMap.put(1, Collections.singletonList(databaseMocker));
        Mockito.when(context.getCachedReplayResultMap()).thenReturn(cachedReplayResultMap);
        assertDoesNotThrow(() -> ReplayUtil.saveRemainCompareResult(context));
    }
}
