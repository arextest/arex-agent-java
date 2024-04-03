package io.arex.inst.dubbo.alibaba;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.util.MockUtils;
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
    static RpcContext context;

    @BeforeAll
    static void setUp() {
        invoker = Mockito.mock(Invoker.class);
        invocation = Mockito.mock(RpcInvocation.class);
        adapter = DubboAdapter.of(invoker, invocation);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(ProtocolUtils.class);
        context = Mockito.mock(RpcContext.class);
        Mockito.mockStatic(RpcContext.class);
        Mockito.when(RpcContext.getContext()).thenReturn(context);
        URL url = Mockito.mock(URL.class);
        Mockito.when(invoker.getUrl()).thenReturn(url);
    }

    @AfterAll
    static void tearDown() {
        invoker = null;
        invocation = null;
        adapter = null;
        context = null;
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
        assertDoesNotThrow(() -> adapter.execute(result, arexMocker));
    }

    static Stream<Arguments> executeCase() {
        RpcResult result = new RpcResult();
        result.setValue("mock");

        Runnable mocker1 = () -> {
            FutureAdapter<Object> future = new FutureAdapter<>(new TestFuture(result));
            Mockito.when(context.getFuture()).thenReturn(future);
        };
        Runnable mocker2 = () -> {
            result.setException(new RuntimeException("mock"));
            FutureAdapter<Object> future = new FutureAdapter<>(new TestFuture(result));
            Mockito.when(context.getFuture()).thenReturn(future);
        };
        Runnable mocker3 = () -> {
            FutureAdapter<Object> future = new FutureAdapter<>(new TestFuture(new RuntimeException("mock")));
            Mockito.when(context.getFuture()).thenReturn(future);
        };
        Runnable mocker4 = () -> {
            Mockito.when(context.getFuture()).thenReturn(null);
        };

        return Stream.of(
                arguments(mocker1, result),
                arguments(mocker2, result),
                arguments(mocker3, result),
                arguments(mocker4, result)
        );
    }

    static class TestFuture implements ResponseFuture {
        private Object value;

        private Exception exception;

        public TestFuture(Object value){
            this.value = value;
        }

        public TestFuture(Exception exception){
            this.exception = exception;
        }

        public Object get() throws RemotingException {
            return value;
        }

        public Object get(int timeoutInMillis) throws RemotingException {
            return value;
        }

        public void setCallback(ResponseCallback callback) {
            if (exception != null) {
                callback.caught(exception);
            } else {
                callback.done(value);
            }
        }

        public boolean isDone() {
            return true;
        }

    }

    @Test
    void getProtocol() {
        assertNull(adapter.getProtocol());
    }

    @Test
    void getConfigVersion() {
        assertNull(adapter.getConfigVersion());
    }

    @ParameterizedTest
    @MethodSource("getRequestHeadersCase")
    void getRequestHeaders(Runnable mocker) {
        mocker.run();
        assertNotNull(adapter.getRequestHeaders());
    }

    static Stream<Arguments> getRequestHeadersCase() {
        Runnable emptyMocker = () -> {};
        Runnable invocationMocker = () -> {
            Mockito.when(invocation.getAttachment(any())).thenReturn("mock");
        };
        Runnable invokerMocker = () -> {
            Mockito.when(invocation.getAttachment(any())).thenReturn("");
            Invoker invoker = Mockito.mock(Invoker.class);
            Mockito.when(invocation.getInvoker()).thenReturn(invoker);
            URL url = Mockito.mock(URL.class);
            Mockito.when(invoker.getUrl()).thenReturn(url);
            Mockito.when(url.getParameter(any())).thenReturn("mock");
        };

        return Stream.of(
                arguments(emptyMocker),
                arguments(invocationMocker),
                arguments(invokerMocker)
        );
    }
}