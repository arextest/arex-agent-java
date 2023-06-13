package io.arex.inst.dubbo.apache.v3.stream;

import io.arex.agent.bootstrap.TraceContextManager;
import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.util.MockUtils;
import org.apache.dubbo.remoting.api.Connection;
import org.apache.dubbo.rpc.CancellationContext;
import org.apache.dubbo.rpc.TriRpcStatus;
import org.apache.dubbo.rpc.model.FrameworkModel;
import org.apache.dubbo.rpc.model.MethodDescriptor;
import org.apache.dubbo.rpc.model.PackableMethod;
import org.apache.dubbo.rpc.protocol.tri.RequestMetadata;
import org.apache.dubbo.rpc.protocol.tri.call.ClientCall;
import org.apache.dubbo.rpc.protocol.tri.call.TripleClientCall;
import org.apache.dubbo.rpc.protocol.tri.stream.ClientStream;
import org.junit.jupiter.api.AfterAll;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DubboStreamConsumerExtractorTest {
    static DubboStreamConsumerExtractor instance;
    static DubboStreamAdapter adapter;
    static MockedStatic<TraceContextManager> traceContextManagerMocker;
    static MockedStatic<MockUtils> mockUtilsMocker;
    static RequestMetadata requestMetadata;

    @BeforeAll
    static void setUp() {
        adapter = Mockito.mock(DubboStreamAdapter.class);
        instance = new DubboStreamConsumerExtractor(adapter);
        traceContextManagerMocker = Mockito.mockStatic(TraceContextManager.class);
        Mockito.mockStatic(DubboStreamCache.class);
        mockUtilsMocker = Mockito.mockStatic(MockUtils.class);
        requestMetadata = Mockito.mock(RequestMetadata.class);
        requestMetadata.method = Mockito.mock(MethodDescriptor.class);
        requestMetadata.packableMethod = Mockito.mock(PackableMethod.class);
    }

    @AfterAll
    static void tearDown() {
        instance = null;
        adapter = null;
        traceContextManagerMocker = null;
        mockUtilsMocker = null;
        requestMetadata = null;
        Mockito.clearAllCaches();
    }

    @Test
    void saveRequest() {
        PackableMethod packableMethod = Mockito.mock(PackableMethod.class);
        instance.saveRequest(packableMethod, null);
        verify(adapter).saveRequest(any());
    }

    @Test
    void init() {
        ClientStream stream = Mockito.mock(ClientStream.class);
        instance.init(stream);
        traceContextManagerMocker.verify(() -> TraceContextManager.set(any()), times(0));
        Mockito.when(DubboStreamCache.getTraceId(any())).thenReturn("mock");
        instance.init(stream);
        traceContextManagerMocker.verify(() -> TraceContextManager.set(any()), times(1));
    }

    @ParameterizedTest
    @MethodSource("recordCase")
    void record(Runnable mocker, Throwable throwable, Assert asserts) {
        mocker.run();
        instance.record(requestMetadata, null, throwable);
        asserts.verity();
    }

    static java.util.stream.Stream<Arguments> recordCase() {
        Runnable emptyMocker = () -> {};
        byte[] data = new byte[]{1};
        Runnable mocker1 = () -> {
            List<StreamModel.DataModel> dataModels = new ArrayList<>();
            StreamModel.DataModel dataModel = StreamModel.DataModel.of(false, data);
            dataModels.add(dataModel);
            Mockito.when(adapter.getRequestMessages()).thenReturn(dataModels);
            ArexMocker arexMocker = new ArexMocker();
            arexMocker.setTargetRequest(new Mocker.Target());
            arexMocker.setTargetResponse(new Mocker.Target());
            Mockito.when(MockUtils.createDubboConsumer(any())).thenReturn(arexMocker);
        };
        Runnable mocker2 = () -> {
            List<StreamModel.DataModel> dataModels = new ArrayList<>();
            StreamModel.DataModel dataModel = StreamModel.DataModel.of(true, data);
            dataModels.add(dataModel);
            Mockito.when(adapter.getRequestMessages()).thenReturn(dataModels);
        };
        Assert asserts1 = () -> {
            mockUtilsMocker.verify(() -> MockUtils.recordMocker(any()), times(0));
        };
        Assert asserts2 = () -> {
            mockUtilsMocker.verify(() -> MockUtils.recordMocker(any()), atLeastOnce());
        };

        return java.util.stream.Stream.of(
                arguments(emptyMocker, null, asserts1),
                arguments(mocker1, new NullPointerException(), asserts2),
                arguments(mocker2, null, asserts2)
        );
    }

    @Test
    void replay() {
        Mockito.when(adapter.replay(any(), any(), any(), anyBoolean())).thenReturn(new ArrayList<>());
        List<MockResult> result = instance.replay(null, requestMetadata);
        assertNotNull(result);
    }

    @Test
    void doReplay() {
        ClientCall.Listener listener = Mockito.mock(ClientCall.Listener.class);
        Connection connection = Mockito.mock(Connection.class);
        Executor executor = Mockito.mock(Executor.class);
        FrameworkModel frameworkModel = Mockito.mock(FrameworkModel.class);
        TripleClientCall clientCall = new TripleClientCall(connection, executor, frameworkModel);
        instance.doReplay(clientCall, listener, Collections.singletonList(MockResult.success(new NullPointerException())));
        verify(listener).onClose(any(), any());
        instance.doReplay(clientCall, listener, Collections.singletonList(MockResult.success("mock")));
        verify(listener).onMessage(any());
    }

    @Test
    void complete() {
        TriRpcStatus status = TriRpcStatus.UNKNOWN;
        instance.complete(status, requestMetadata);
        verify(adapter).clearRequest();
    }

    @Test
    void close() {
        ClientCall.Listener listener = Mockito.mock(ClientCall.Listener.class);
        CancellationContext cancellationContext = Mockito.mock(CancellationContext.class);
        requestMetadata.cancellationContext = cancellationContext;
        DubboStreamConsumerExtractor.close(listener, requestMetadata);
        verify(requestMetadata.cancellationContext).cancel(any());
    }

    interface Assert {
        void verity();
    }
}