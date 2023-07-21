package io.arex.inst.dubbo.apache.v2;

import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.util.IgnoreUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DubboProviderExtractorTest {
    static DubboAdapter adapter;
    static RpcInvocation invocation;

    @BeforeAll
    static void setUp() {
        adapter = Mockito.mock(DubboAdapter.class);
        invocation = Mockito.mock(RpcInvocation.class);
        Mockito.mockStatic(DubboAdapter.class);
        Mockito.when(DubboAdapter.of(any(), any())).thenReturn(adapter);
        Mockito.mockStatic(CaseEventDispatcher.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RpcContext.class);
        Mockito.mockStatic(RecordLimiter.class);
        Mockito.mockStatic(IgnoreUtils.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
    }

    @AfterAll
    static void tearDown() {
        adapter = null;
        invocation = null;
        Mockito.clearAllCaches();
    }

    @Test
    void onServiceEnter() {
        Mockito.when(IgnoreUtils.ignoreOperation(any())).thenReturn(false);
        DubboProviderExtractor.onServiceEnter(null, invocation);
    }

    @ParameterizedTest
    @MethodSource("onServiceExitCase")
    void onServiceExit(Runnable mocker, Runnable asserts) {
        mocker.run();
        DubboProviderExtractor.onServiceExit(null, invocation, null);
        asserts.run();
    }

    static Stream<Arguments> onServiceExitCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            RpcContext rpcContext = Mockito.mock(RpcContext.class);
            Mockito.when(RpcContext.getServerContext()).thenReturn(rpcContext);
            ArexContext arexContext = Mockito.mock(ArexContext.class);
            Mockito.when(ContextManager.currentContext()).thenReturn(arexContext);
            Mockito.when(RpcContext.getContext()).thenReturn(rpcContext);
        };
        Runnable asserts1 = () -> verify(adapter, times(0)).execute(any(), any());
        Runnable asserts2 = () -> verify(adapter, times(1)).execute(any(), any());
        return Stream.of(
                arguments(emptyMocker, asserts1),
                arguments(mocker1, asserts2)
        );
    }
}