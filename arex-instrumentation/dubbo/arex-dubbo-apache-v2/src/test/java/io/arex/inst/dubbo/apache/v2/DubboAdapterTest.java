package io.arex.inst.dubbo.apache.v2;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.util.MockUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.support.ProtocolUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DubboAdapterTest {
    static DubboAdapter adapter;
    static Invoker<String> invoker;
    static RpcInvocation invocation;

    @BeforeAll
    static void setUp() {
        invoker = Mockito.mock(Invoker.class);
        invocation = Mockito.mock(RpcInvocation.class);
        adapter = DubboAdapter.of(invoker, invocation);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(ProtocolUtils.class);
        URL url = Mockito.mock(URL.class);
        Mockito.when(invoker.getUrl()).thenReturn(url);
    }

    @AfterAll
    static void tearDown() {
        invoker = null;
        invocation = null;
        adapter = null;
        Mockito.clearAllCaches();
    }

    @Test
    void getServiceName() {
        adapter.getServiceName();
        Mockito.when(invoker.getInterface()).thenReturn(String.class);
        adapter.getServiceName();
        verify(invoker, atLeastOnce()).getInterface();
    }

    @Test
    void getPath() {
        adapter.getPath();
        verify(invocation, atLeastOnce()).getAttachment(any());
    }

    @Test
    void getOperationName() {
        adapter.getOperationName();
        verify(invocation).getMethodName();
    }

    @Test
    void getServiceOperation() {
        adapter.getServiceOperation();
        verify(invocation, atLeastOnce()).getMethodName();
    }

    @Test
    void getRequest() {
        Mockito.when(invocation.getArguments()).thenReturn(new Object[]{"mock"});
        assertNull(adapter.getRequest());
    }

    @Test
    void getRequestParamType() {
        Mockito.when(invocation.getParameterTypes()).thenReturn(new Class<?>[]{String.class});
        assertNotNull(adapter.getRequestParamType());
    }

    @Test
    void getRecordRequestType() {
        Mockito.when(invocation.getArguments()).thenReturn(new Object[]{"mock"});
        assertNotNull(adapter.getRecordRequestType());
    }

    @Test
    void getUrl() {
        adapter.getUrl();
        verify(invoker, atLeastOnce()).getUrl();
    }

    @Test
    void getGeneric() {
        Mockito.when(invocation.getAttachment("generic")).thenReturn(null);
        assertNull(adapter.getGeneric());
    }

    @Test
    void getCaseId() {
        adapter.getCaseId();
        verify(invocation, atLeastOnce()).getAttachment(any());
    }

    @Test
    void getExcludeMockTemplate() {
        adapter.getExcludeMockTemplate();
        verify(invocation, atLeastOnce()).getAttachment(any());
    }

    @Test
    void getInvocation() {
        assertNotNull(adapter.getInvocation());
    }

    @Test
    void forceRecord() {
        assertFalse(adapter.forceRecord());
    }

    @Test
    void replayWarmUp() {
        assertFalse(adapter.replayWarmUp());
    }

    @ParameterizedTest
    @MethodSource("executeCase")
    void execute(Runnable mocker, Result result) {
        mocker.run();
        ArexMocker arexMocker = new ArexMocker();
        arexMocker.setTargetResponse(new Mocker.Target());
        assertNotNull(adapter.execute(result, arexMocker));
    }

    static Stream<Arguments> executeCase() {
        AsyncRpcResult result1 = new AsyncRpcResult(invocation);
        result1.setValue(null);

        AsyncRpcResult result2 = new AsyncRpcResult(invocation);
        result2.setValue(new AppResponse("mock"));

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("key", "val");
        AsyncRpcResult result3 = new AsyncRpcResult(invocation);
        result3.setValue(new AppResponse(responseMap));

        CompletableFuture<AppResponse> future4 = CompletableFuture.completedFuture(null);
        future4 = future4.thenApply(r -> {
            throw new NullPointerException();
        });
        AsyncRpcResult result4 = new AsyncRpcResult(invocation);
        result4.subscribeTo(future4);

        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(invocation.getAttachment("generic")).thenReturn("true");
            Mockito.when(ProtocolUtils.isGeneric(any())).thenReturn(false);
        };
        Runnable mocker2 = () -> {
            Mockito.when(ProtocolUtils.isGeneric(any())).thenReturn(true);
        };
        Runnable mocker3 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };

        return Stream.of(
                arguments(emptyMocker, result1),
                arguments(mocker1, result2),
                arguments(mocker2, result2),
                arguments(mocker3, result3),
                arguments(emptyMocker, result4)
        );
    }

    @Test
    void getProtocol() {
        assertNull(adapter.getProtocol());
    }

    @Test
    void getConfigVersion() {
        assertNull(adapter.getConfigVersion());
    }
}