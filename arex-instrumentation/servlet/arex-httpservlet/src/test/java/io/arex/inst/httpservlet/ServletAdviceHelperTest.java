package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.runtime.util.IgnoreUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.support.InvocableHandlerMethod;

import javax.servlet.http.HttpServletRequest;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ServletAdviceHelperTest {
    static ServletAdapter adapter;
    static HttpServletRequest request;
    static InvocableHandlerMethod invocableHandlerMethod;

    @BeforeAll
    static void setUp() {
        adapter = Mockito.mock(ServletAdapter.class);
        request = Mockito.mock(HttpServletRequest.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RecordLimiter.class);
        Mockito.mockStatic(CaseEventDispatcher.class);
        invocableHandlerMethod = Mockito.mock(InvocableHandlerMethod.class);
        Mockito.mockStatic(IgnoreUtils.class);
    }

    @AfterAll
    static void tearDown() {
        adapter = null;
        request = null;
        invocableHandlerMethod = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("onServiceEnterCase")
    void onServiceEnter(Runnable mocker, Predicate<Pair> predicate) {
        mocker.run();
        Pair result = ServletAdviceHelper.onServiceEnter(adapter, new Object(), new Object());
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> onServiceEnterCase() {
        Runnable mocker1 = () -> {
            Mockito.when(adapter.getAttribute(any(), any())).thenReturn("true");
        };
        Runnable mocker2 = () -> {
            Mockito.when(adapter.getAttribute(any(), any())).thenReturn("false");
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.RECORD_ID))).thenReturn("mock");
        };
        Runnable mocker3 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.RECORD_ID))).thenReturn("");
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.FORCE_RECORD))).thenReturn("true");
        };
        Runnable mocker4 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.FORCE_RECORD))).thenReturn("false");
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.REPLAY_WARM_UP))).thenReturn("true");
        };
        Runnable mocker5 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.REPLAY_WARM_UP))).thenReturn("false");
            Mockito.when(adapter.getMethod(any())).thenReturn("GET");
            Mockito.when(adapter.getFullUrl(any())).thenReturn(".png");
        };
        Runnable mocker6 = () -> {
            Mockito.when(adapter.getMethod(any())).thenReturn("POST");
            Mockito.when(adapter.getContentType(any())).thenReturn("image/");
        };
        Runnable mocker7 = () -> {
            Mockito.when(adapter.getContentType(any())).thenReturn("mock");
            Mockito.when(adapter.getRequestURI(any())).thenReturn("uri");
        };
        Runnable mocker7_1 = () -> {
            Mockito.when(IgnoreUtils.ignoreOperation(any())).thenReturn(true);
        };
        Runnable mocker8 = () -> {
            Mockito.when(IgnoreUtils.ignoreOperation(any())).thenReturn(false);
            Mockito.when(RecordLimiter.acquire(any())).thenReturn(true);
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        };

        Predicate<Pair<?, ?>> predicate1 = Objects::isNull;
        Predicate<Pair<?, ?>> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(mocker1, predicate2),
                arguments(mocker2, predicate1),
                arguments(mocker3, predicate1),
                arguments(mocker4, predicate1),
                arguments(mocker5, predicate1),
                arguments(mocker6, predicate1),
                arguments(mocker7, predicate1),
                arguments(mocker7_1, predicate1),
                arguments(mocker8, predicate2)
        );
    }

    @ParameterizedTest
    @MethodSource("onServiceExitCase")
    void onServiceExit(Runnable mocker) {
        try (MockedConstruction<ServletExtractor> mocked = Mockito.mockConstruction(ServletExtractor.class, (mock, context) -> {
            System.out.println("mock ServletExtractor");
        })) {
            mocker.run();
            assertDoesNotThrow(() -> ServletAdviceHelper.onServiceExit(adapter, new Object(), new Object()));
        }
    }

    static Stream<Arguments> onServiceExitCase() {
        Runnable mocker1 = () -> {
            Mockito.when(adapter.wrapped(any(), any())).thenReturn(false);
        };
        Runnable mocker2 = () -> {
            Mockito.when(adapter.wrapped(any(), any())).thenReturn(true);
            Mockito.when(adapter.getStatus(any())).thenReturn(100);
        };
        Runnable mocker3 = () -> {
            Mockito.when(adapter.getStatus(any())).thenReturn(200);
            Mockito.when(adapter.getAttribute(any(), eq(ServletAdviceHelper.SERVLET_ASYNC_FLAG))).thenReturn("true");
        };
        Runnable mocker4 = () -> {
            Mockito.when(adapter.getAttribute(any(), eq(ServletAdviceHelper.SERVLET_ASYNC_FLAG))).thenReturn("false");
            Mockito.when(adapter.isAsyncStarted(any())).thenReturn(true);
        };
        Runnable mocker5 = () -> {
            Mockito.when(adapter.isAsyncStarted(any())).thenReturn(false);
        };

        return Stream.of(
                arguments(mocker1),
                arguments(mocker2),
                arguments(mocker3),
                arguments(mocker4),
                arguments(mocker5)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("onInvokeForRequestExitCase")
    void onInvokeForRequestExit(String log, Runnable mocker, InvocableHandlerMethod invocableHandlerMethod, Object response, Predicate<Object> predicate) {
        mocker.run();
        ServletAdviceHelper.onInvokeForRequestExit(adapter, null, invocableHandlerMethod, response);
        assertTrue(predicate.test(response));
    }

    static Stream<Arguments> onInvokeForRequestExitCase() {
        MethodParameter parameter = Mockito.mock(MethodParameter.class);

        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            Mockito.when(invocableHandlerMethod.getReturnType()).thenReturn(parameter);
            Class mockBeanType = ServletAdviceHelperTest.class;
            Mockito.when(invocableHandlerMethod.getBeanType()).thenReturn(mockBeanType);
        };
        Runnable mocker3 = () -> {
            Mockito.when(parameter.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
        };
        Runnable mocker4 = () -> {
            Mockito.when(parameter.hasMethodAnnotation(ResponseBody.class)).thenReturn(false);
            Class mockBeanType = TestRestController.class;
            Mockito.when(invocableHandlerMethod.getBeanType()).thenReturn(mockBeanType);
        };
        Runnable mocker5 = () -> {
            Mockito.when(adapter.getNativeRequest(any())).thenReturn(request);
        };

        Predicate<Object> predicate1 = Objects::isNull;
        Predicate<Object> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments("response is null", emptyMocker, null, null, predicate1),
                arguments("record or replay is false", emptyMocker, null, CompletableFuture.completedFuture("mock"), predicate2),
                arguments("response is CompletableFuture" , mocker1, null, CompletableFuture.completedFuture("mock"), predicate2),
                arguments("returnType or beanType not match", mocker2, invocableHandlerMethod, "mock", predicate2),
                arguments("returnType has ResponseBody", mocker3, invocableHandlerMethod, "mock", predicate2),
                arguments("beanType has RestController", mocker4, invocableHandlerMethod, "mock", predicate2),
                arguments("native request not null", mocker5, invocableHandlerMethod, "mock", predicate2)
        );
    }

    @RestController
    public static class TestRestController {

    }
}
