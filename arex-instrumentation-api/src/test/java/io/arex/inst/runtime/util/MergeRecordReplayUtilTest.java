package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.Assert;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.sizeof.AgentSizeOf;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MergeRecordReplayUtilTest {
    static MockedStatic<MockUtils> mockUtils;
    static AgentSizeOf agentSizeOf;
    static ArexMocker requestMocker;

    @BeforeAll
    static void setUp() {
        agentSizeOf = Mockito.mock(AgentSizeOf.class);
        Mockito.mockStatic(AgentSizeOf.class);
        Mockito.when(AgentSizeOf.newInstance()).thenReturn(agentSizeOf);
        mockUtils = Mockito.mockStatic(MockUtils.class);
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
        mockUtils = null;
        agentSizeOf = null;
        requestMocker = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("mergeRecordCase")
    void mergeRecord(Runnable mocker, Mocker requestMocker, Assert asserts) {
        mocker.run();
        MergeRecordReplayUtil.mergeRecord(requestMocker);
        assertDoesNotThrow(asserts::verity);
    }

    static Stream<Arguments> mergeRecordCase() {
        Runnable emptyMocker = () -> {};
        ArexContext context = Mockito.mock(ArexContext.class);
        LinkedBlockingQueue<MergeDTO> mergeRecordQueue = new LinkedBlockingQueue<>(1);
        Runnable mocker1 = () -> {
            mergeRecordQueue.offer(new MergeDTO());
            Mockito.when(context.getMergeRecordQueue()).thenReturn(mergeRecordQueue);
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
        };
        Runnable mocker2 = () -> {
            mergeRecordQueue.poll();
            Mockito.when(Config.get().getInt(ArexConstants.MERGE_RECORD_THRESHOLD, ArexConstants.MERGE_RECORD_THRESHOLD_DEFAULT))
                    .thenReturn(ArexConstants.MERGE_RECORD_THRESHOLD_DEFAULT);
        };
        Runnable mocker3 = () -> {
            mergeRecordQueue.poll();
            Mockito.when(Config.get().getInt(ArexConstants.MERGE_RECORD_THRESHOLD, ArexConstants.MERGE_RECORD_THRESHOLD_DEFAULT))
                    .thenReturn(0);
        };
        Runnable mocker4 = () -> {
            mergeRecordQueue.poll();
            Mockito.when(Config.get().getInt(ArexConstants.MERGE_RECORD_THRESHOLD, ArexConstants.MERGE_RECORD_THRESHOLD_DEFAULT))
                    .thenReturn(1);
            Mockito.when(agentSizeOf.checkMemorySizeLimit(any(), any(long.class))).thenReturn(true);
        };
        Runnable mocker5 = () -> {
            mergeRecordQueue.poll();
            Mockito.when(agentSizeOf.checkMemorySizeLimit(any(), any(long.class))).thenReturn(false);
        };
        Assert asserts1 = () -> {
            mockUtils.verify(() -> MockUtils.executeRecord(any()), times(0));
        };
        Assert asserts2 = () -> {
            mockUtils.verify(() -> MockUtils.executeRecord(any()), atLeastOnce());
        };
        return Stream.of(
                arguments(emptyMocker, requestMocker, asserts1),
                arguments(mocker1, requestMocker, asserts1),
                arguments(mocker2, requestMocker, asserts1),
                arguments(mocker3, requestMocker, asserts1),
                arguments(mocker4, requestMocker, asserts2),
                arguments(mocker5, requestMocker, asserts2)
        );
    }

    @ParameterizedTest
    @MethodSource("recordRemainCase")
    void recordRemain(ArexContext context, Predicate<ArexContext> asserts) {
        MergeRecordReplayUtil.recordRemain(context);
        asserts.test(context);
    }

    static Stream<Arguments> recordRemainCase() {
        Supplier<ArexContext> contextSupplier1 = () -> ArexContext.of("mock");
        Supplier<ArexContext> contextSupplier2 = () -> {
            ArexContext arexContext = contextSupplier1.get();
            arexContext.getMergeRecordQueue().offer(new MergeDTO());
            return arexContext;
        };

        Predicate<ArexContext> asserts1 = Objects::isNull;
        Predicate<ArexContext> asserts2 = Objects::nonNull;
        return Stream.of(
                arguments(null, asserts1),
                arguments(contextSupplier1.get(), asserts2),
                arguments(contextSupplier2.get(), asserts2)
        );
    }

    @ParameterizedTest
    @MethodSource("mergeReplayCase")
    void mergeReplay(Runnable mocker) {
        mocker.run();
        assertDoesNotThrow(MergeRecordReplayUtil::mergeReplay);
    }

    static Stream<Arguments> mergeReplayCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            ArexContext context = Mockito.mock(ArexContext.class);
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
            Map<Integer, List<MergeDTO>> cachedReplayResultMap = new HashMap<>();
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