package io.arex.inst.dubbo.stream;

import io.arex.agent.bootstrap.cache.DubboStreamCache;
import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.model.StreamModel;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.util.MockUtils;
import org.apache.dubbo.rpc.model.MethodDescriptor;
import org.apache.dubbo.rpc.protocol.tri.stream.Stream;
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

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DubboStreamAdapterTest {
    static DubboStreamAdapter adapter;
    static MockedStatic<DubboStreamCache> dubboStreamCacheMocker;

    @BeforeAll
    static void setUp() {
        adapter = DubboStreamAdapter.of(Mockito.mock(Stream.class));
        dubboStreamCacheMocker = Mockito.mockStatic(DubboStreamCache.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(Config.class);
    }

    @AfterAll
    static void tearDown() {
        adapter = null;
        Mockito.clearAllCaches();
    }

    @Test
    void saveRequest() {
        Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock"));
        adapter.saveRequest(null);
        dubboStreamCacheMocker.verify(() -> DubboStreamCache.put(any(), any(), any()));
    }

    @Test
    void getRequestMessages() {
        StreamModel streamModel = new StreamModel("mock", null);
        Mockito.when(DubboStreamCache.get(any())).thenReturn(streamModel);
        assertNotNull(adapter.getRequestMessages());
    }

    @ParameterizedTest
    @MethodSource("replayCase")
    void replay(Runnable mocker, MethodDescriptor.RpcType rpcType, Predicate<List<MockResult>> predicate) {
        mocker.run();
        ArexMocker arexMocker = new ArexMocker();
        arexMocker.setTargetRequest(new Mocker.Target());
        List<MockResult> result = adapter.replay(arexMocker, null, rpcType, false);
        assertTrue(predicate.test(result));
    }

    static java.util.stream.Stream<Arguments> replayCase() {
        Runnable mocker1 = () -> {
            Config config = Mockito.mock(Config.class);
            Mockito.when(config.getDubboStreamReplayThreshold()).thenReturn(1);
            Mockito.when(Config.get()).thenReturn(config);
        };
        Runnable mocker2 = () -> {
            Mockito.when(MockUtils.replayBody(any(), any())).thenReturn("mock");
        };
        Predicate<List<MockResult>> predicate1 = Objects::nonNull;
        return java.util.stream.Stream.of(
                arguments(mocker1, MethodDescriptor.RpcType.BI_STREAM, predicate1),
                arguments(mocker2, MethodDescriptor.RpcType.SERVER_STREAM, predicate1)
        );
    }

    @Test
    void clearRequest() {
        adapter.clearRequest();
        dubboStreamCacheMocker.verify(() -> DubboStreamCache.remove(any()));
    }
}