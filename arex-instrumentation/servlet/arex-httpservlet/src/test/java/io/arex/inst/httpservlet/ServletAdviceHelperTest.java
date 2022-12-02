package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.foundation.context.ContextManager;
import io.arex.foundation.listener.CaseInitializer;
import io.arex.foundation.model.Constants;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServletAdviceHelperTest {
    static ServletAdviceHelper target;
    static ServletAdapter adapter;
    static HttpServletRequest request;
    static InvocableHandlerMethod invocableHandlerMethod;

    @BeforeAll
    static void setUp() {
        target = new ServletAdviceHelper();
        adapter = Mockito.mock(ServletAdapter.class);
        request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(adapter.asHttpServletRequest(any())).thenReturn(request);
        Mockito.mockStatic(CaseInitializer.class);
        Mockito.mockStatic(ContextManager.class);
        invocableHandlerMethod = Mockito.mock(InvocableHandlerMethod.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        adapter = null;
        request = null;
        invocableHandlerMethod = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("onServiceEnterCase")
    void onServiceEnter(Runnable mocker, Predicate<Pair> predicate) {
        mocker.run();
        Pair result = target.onServiceEnter(adapter, new Object(), new Object());
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> onServiceEnterCase() {
        Runnable mocker1 = () -> {
            Mockito.when(adapter.getAttribute(any(), any())).thenReturn("true");
        };
        Runnable mocker2 = () -> {
            Mockito.when(adapter.getAttribute(any(), any())).thenReturn("false");
            Mockito.when(adapter.getRequestHeader(any(), eq(Constants.RECORD_ID))).thenReturn("mock");
        };
        Runnable mocker3 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(Constants.RECORD_ID))).thenReturn("");
            Mockito.when(adapter.getRequestHeader(any(), eq(Constants.FORCE_RECORD))).thenReturn("true");
        };
        Runnable mocker4 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(Constants.FORCE_RECORD))).thenReturn("false");
            Mockito.when(adapter.getRequestHeader(any(), eq(Constants.REPLAY_WARM_UP))).thenReturn("true");
        };
        Runnable mocker5 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(Constants.REPLAY_WARM_UP))).thenReturn("false");
            Mockito.when(adapter.getMethod(any())).thenReturn("GET");
            Mockito.when(adapter.getServletPath(any())).thenReturn("mock");
        };
        Runnable mocker6 = () -> {
            Mockito.when(adapter.getMethod(any())).thenReturn("POST");
        };
        Runnable mocker7 = () -> {
            Mockito.when(adapter.getContentType(any())).thenReturn("mock");
        };
        Runnable mocker8 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        };
        Runnable mocker9 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
        };

        Predicate<Pair> predicate1 = Objects::isNull;
        Predicate<Pair> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(mocker1, predicate2),
                arguments(mocker2, predicate1),
                arguments(mocker3, predicate1),
                arguments(mocker4, predicate1),
                arguments(mocker5, predicate1),
                arguments(mocker6, predicate1),
                arguments(mocker7, predicate1),
                arguments(mocker8, predicate2),
                arguments(mocker9, predicate1)
        );
    }

    @ParameterizedTest
    @MethodSource("onServiceExitCase")
    void onServiceExit(Runnable mocker) {
        mocker.run();
        target.onServiceExit(adapter, new Object(), new Object());
        verify(adapter, atLeastOnce()).asHttpServletRequest(any());
    }

    static Stream<Arguments> onServiceExitCase() {
        Runnable mocker1 = () -> {
            Mockito.when(adapter.getAttribute(any(), any())).thenReturn("true");
        };
        Runnable mocker2 = () -> {
            Mockito.when(adapter.wrapped(any(), any())).thenReturn(true);
        };
        Runnable mocker3 = () -> {
            Mockito.when(adapter.getStatus(any())).thenReturn(HttpStatus.SC_CONTINUE);
        };
        Runnable mocker4 = () -> {
            Mockito.when(adapter.getStatus(any())).thenReturn(HttpStatus.SC_OK);
            Mockito.when(adapter.getAttribute(any(), eq(ServletConstants.SERVLET_ASYNC_FLAG))).thenReturn("true");
        };
        Runnable mocker5 = () -> {
            Mockito.when(adapter.getAttribute(any(), eq(ServletConstants.SERVLET_ASYNC_FLAG))).thenReturn("false");
            Mockito.when(adapter.isAsyncStarted(any())).thenReturn(true);
        };
        Runnable mocker6 = () -> {
            Mockito.when(adapter.isAsyncStarted(any())).thenReturn(false);
        };

        return Stream.of(
                arguments(mocker1),
                arguments(mocker2),
                arguments(mocker3),
                arguments(mocker4),
                arguments(mocker5),
                arguments(mocker6)
        );
    }

    @ParameterizedTest
    @MethodSource("onInvokeForRequestExitCase")
    void onInvokeForRequestExit(Runnable mocker, InvocableHandlerMethod invocableHandlerMethod, Object response, Predicate<Object> predicate) {
        mocker.run();
        target.onInvokeForRequestExit(adapter, null, invocableHandlerMethod, response);
        assertTrue(predicate.test(response));
    }

    static Stream<Arguments> onInvokeForRequestExitCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            MethodParameter parameter = Mockito.mock(MethodParameter.class);
            Mockito.when(invocableHandlerMethod.getReturnType()).thenReturn(parameter);
        };
        Runnable mocker3 = () -> {
            MethodParameter parameter = Mockito.mock(MethodParameter.class);
            Mockito.when(parameter.hasMethodAnnotation(any())).thenReturn(true);
            Mockito.when(invocableHandlerMethod.getReturnType()).thenReturn(parameter);
        };
        Runnable mocker4 = () -> {
            Mockito.when(adapter.getNativeRequest(any())).thenReturn(request);
        };
        Predicate<Object> predicate1 = Objects::isNull;
        Predicate<Object> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(emptyMocker, null, null, predicate1),
                arguments(emptyMocker, null, CompletableFuture.completedFuture("mock"), predicate2),
                arguments(mocker1, null, CompletableFuture.completedFuture("mock"), predicate2),
                arguments(mocker2, invocableHandlerMethod, "mock", predicate2),
                arguments(mocker3, invocableHandlerMethod, "mock", predicate2),
                arguments(mocker4, invocableHandlerMethod, "mock", predicate2)
        );
    }
}