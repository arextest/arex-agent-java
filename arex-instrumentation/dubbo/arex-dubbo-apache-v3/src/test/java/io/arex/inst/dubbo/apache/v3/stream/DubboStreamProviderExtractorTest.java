package io.arex.inst.dubbo.apache.v3.stream;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.util.MockUtils;
import org.apache.dubbo.rpc.TriRpcStatus;
import org.apache.dubbo.rpc.model.MethodDescriptor;
import org.apache.dubbo.rpc.model.PackableMethod;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DubboStreamProviderExtractorTest {
    static DubboStreamProviderExtractor instance;
    static DubboStreamAdapter adapter;
    static MockedStatic<MockUtils> mockUtilsMocker;
    static PackableMethod packableMethod;
    static MethodDescriptor methodDescriptor;

    @BeforeAll
    static void setUp() {
        adapter = Mockito.mock(DubboStreamAdapter.class);
        instance = new DubboStreamProviderExtractor(adapter);
        Mockito.mockStatic(DubboStreamCache.class);
        mockUtilsMocker = Mockito.mockStatic(MockUtils.class);
        packableMethod = Mockito.mock(PackableMethod.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock"));
        methodDescriptor = Mockito.mock(MethodDescriptor.class);
    }

    @AfterAll
    static void tearDown() {
        instance = null;
        adapter = null;
        mockUtilsMocker = null;
        Mockito.clearAllCaches();
    }

    @Test
    void saveRequest() {
        instance.saveRequest(null);
        verify(adapter).saveRequest(any());
    }

    @Test
    void record() {
        List<StreamModel.DataModel> dataModels = new ArrayList<>();
        byte[] data = new byte[]{1};
        StreamModel.DataModel dataModel = StreamModel.DataModel.of(false, data);
        dataModels.add(dataModel);
        Mockito.when(adapter.getRequestMessages()).thenReturn(dataModels);
        // throw exception
        Assertions.assertDoesNotThrow(() -> instance.record(new HashMap<>(), null, null,
                methodDescriptor, packableMethod));

        ArexMocker arexMocker = new ArexMocker();
        arexMocker.setTargetRequest(new Mocker.Target());
        arexMocker.setTargetResponse(new Mocker.Target());
        Mockito.when(MockUtils.createDubboStreamProvider(any())).thenReturn(arexMocker);

        instance.record(new HashMap<>(), null, null, methodDescriptor, packableMethod);
        mockUtilsMocker.verify(() -> MockUtils.recordMocker(any()), times(1));

        dataModel.setRecorded(true);
        instance.record(new HashMap<>(), null, null, methodDescriptor, packableMethod);
        mockUtilsMocker.verify(() -> MockUtils.recordMocker(any()), times(2));
    }

    @Test
    void replay() {
        Mockito.when(adapter.replay(any(), any(), any(), anyBoolean())).thenReturn(new ArrayList<>());
        instance.replay(null, null, methodDescriptor, packableMethod);
        verify(adapter).replay(any(), any(), any(), anyBoolean());
    }

    @ParameterizedTest
    @MethodSource("completeCase")
    void complete(Runnable mocker) {
        mocker.run();
        TriRpcStatus status = TriRpcStatus.UNKNOWN;
        instance.complete(status, null, null, methodDescriptor, packableMethod);
        verify(adapter, atLeastOnce()).clearRequest();
    }

    static java.util.stream.Stream<Arguments> completeCase() {
        byte[] data = new byte[]{1};
        Runnable mocker1 = () -> {
            ArexMocker arexMocker = new ArexMocker();
            arexMocker.setTargetRequest(new Mocker.Target());
            arexMocker.setTargetResponse(new Mocker.Target());
            Mockito.when(MockUtils.createDubboStreamProvider(any())).thenReturn(arexMocker);
        };
        Runnable mocker2 = () -> {
            List<StreamModel.DataModel> dataModels = new ArrayList<>();
            StreamModel.DataModel dataModel = StreamModel.DataModel.of(false, data);
            dataModels.add(dataModel);
            Mockito.when(adapter.getRequestMessages()).thenReturn(dataModels);
        };
        Runnable mocker3 = () -> {
            List<StreamModel.DataModel> dataModels = new ArrayList<>();
            StreamModel.DataModel dataModel = StreamModel.DataModel.of(true, data);
            dataModels.add(dataModel);
            Mockito.when(adapter.getRequestMessages()).thenReturn(dataModels);
        };
        return java.util.stream.Stream.of(
                arguments(mocker1),
                arguments(mocker2),
                arguments(mocker3)
        );
    }
}