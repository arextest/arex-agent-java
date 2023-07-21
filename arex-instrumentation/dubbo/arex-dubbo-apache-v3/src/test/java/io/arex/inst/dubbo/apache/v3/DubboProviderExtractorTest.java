package io.arex.inst.dubbo.apache.v3;

import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.util.IgnoreUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcContextAttachment;
import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

    @ParameterizedTest
    @MethodSource("onServiceEnterCase")
    void onServiceEnter(Runnable mocker, Runnable asserts) {
        mocker.run();
        DubboProviderExtractor.onServiceEnter(null, invocation);
        asserts.run();
    }

    static Stream<Arguments> onServiceEnterCase() {
        Runnable mocker0 = () -> {
            Mockito.when(adapter.getServiceOperation()).thenReturn("org.apache.dubbo.metadata.MetadataService.getMetadataInfo");
        };
        Runnable mocker1 = () -> {
            Mockito.when(adapter.getServiceOperation()).thenReturn("mock");
            Mockito.when(adapter.getCaseId()).thenReturn("mock");
        };
        Runnable mocker2 = () -> {
            Mockito.when(adapter.getCaseId()).thenReturn("");
            Mockito.when(adapter.forceRecord()).thenReturn(true);
        };
        Runnable mocker3 = () -> {
            Mockito.when(adapter.forceRecord()).thenReturn(false);
            Mockito.when(adapter.replayWarmUp()).thenReturn(true);
        };
        Runnable mocker4 = () -> {
            Mockito.when(adapter.replayWarmUp()).thenReturn(false);
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(true);
        };
        Runnable mocker5 = () -> {
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(false);
        };
        Runnable asserts1 = () -> verify(adapter, times(0)).getExcludeMockTemplate();
        Runnable asserts2 = () -> verify(adapter, atLeastOnce()).getExcludeMockTemplate();
        return Stream.of(
                arguments(mocker0, asserts1),
                arguments(mocker1, asserts2),
                arguments(mocker2, asserts2),
                arguments(mocker3, asserts2),
                arguments(mocker4, asserts2),
                arguments(mocker5, asserts2)
        );
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
            RpcContextAttachment rpcContext = Mockito.mock(RpcContextAttachment.class);
            Mockito.when(RpcContext.getServerContext()).thenReturn(rpcContext);
            ArexContext arexContext = Mockito.mock(ArexContext.class);
            Mockito.when(ContextManager.currentContext()).thenReturn(arexContext);
            Mockito.when(RpcContext.getClientAttachment()).thenReturn(rpcContext);
            Mockito.when(RpcContext.getServerAttachment()).thenReturn(rpcContext);
        };
        Runnable asserts1 = () -> verify(adapter, times(0)).execute(any(), any());
        Runnable asserts2 = () -> verify(adapter, times(1)).execute(any(), any());
        return Stream.of(
                arguments(emptyMocker, asserts1),
                arguments(mocker1, asserts2)
        );
    }
}