package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RecordLimiter;
import io.arex.inst.runtime.listener.CaseEventDispatcher;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.runtime.util.IgnoreUtils;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
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
    static HttpServletResponse response;
    static InvocableHandlerMethod invocableHandlerMethod;

    @BeforeAll
    static void setUp() {
        adapter = Mockito.mock(ServletAdapter.class);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RecordLimiter.class);
        Mockito.mockStatic(CaseEventDispatcher.class);
        invocableHandlerMethod = Mockito.mock(InvocableHandlerMethod.class);
        Mockito.mockStatic(IgnoreUtils.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
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
        Runnable emptyMocker = () -> {};
        Runnable mocker01 = () -> {
            Mockito.when(adapter.asHttpServletRequest(any())).thenReturn("mock");
            Mockito.when(adapter.markProcessed(any(), any())).thenReturn(true);
        };
        Runnable mocker02 = () -> {
            Mockito.when(adapter.markProcessed(any(), any())).thenReturn(false);
        };
        Runnable mocker1 = () -> {
            Mockito.when(adapter.asHttpServletResponse(any())).thenReturn("mock");
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
            Mockito.when(adapter.getRequestURI(any())).thenReturn(".png");
        };
        Runnable mocker6 = () -> {
            Mockito.when(adapter.getRequestURI(any())).thenReturn("uri");
            Mockito.when(adapter.getContentType(any())).thenReturn("image/");
        };
        Runnable mocker7 = () -> {
            Mockito.when(adapter.getContentType(any())).thenReturn("mock");
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
                arguments(emptyMocker, predicate1),
                arguments(mocker01, predicate1),
                arguments(mocker02, predicate1),
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
    void onServiceExit(Runnable mocker, Runnable verify) {
        try (MockedConstruction<ServletExtractor> mocked = Mockito.mockConstruction(ServletExtractor.class, (mock, context) -> {
            System.out.println("mock ServletExtractor");
        })) {
            mocker.run();
            ServletAdviceHelper.onServiceExit(adapter, request, response);
            assertDoesNotThrow(verify::run);
        }
    }

    static Stream<Arguments> onServiceExitCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(adapter.asHttpServletRequest(any())).thenReturn(request);
            Mockito.when(adapter.asHttpServletResponse(any())).thenReturn(response);
            Mockito.when(adapter.wrapped(any(), any())).thenReturn(false);
        };

        Runnable mocker2 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
            Mockito.when(adapter.wrapped(any(), any())).thenReturn(true);
        };
        Runnable mocker3 = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(adapter.getStatus(any())).thenReturn(100);
        };
        Runnable mocker4 = () -> {
            Mockito.when(adapter.getStatus(any())).thenReturn(200);
            Mockito.when(adapter.getAttribute(any(), eq(ServletAdviceHelper.SERVLET_ASYNC_FLAG))).thenReturn("true");
        };
        Runnable mocker5 = () -> {
            Mockito.when(adapter.getAttribute(any(), eq(ServletAdviceHelper.SERVLET_ASYNC_FLAG))).thenReturn("false");
            Mockito.when(adapter.isAsyncStarted(any())).thenReturn(true);
        };
        Runnable mocker6 = () -> {
            Mockito.when(adapter.isAsyncStarted(any())).thenReturn(false);
        };
        Runnable mockThrowException = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
            try {
                Mockito.doThrow(new IOException("mock io exception")).when(adapter).copyBodyToResponse(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable verifyEmpty = () -> { };
        Runnable verifyCopyBody = () -> {
            try {
                Mockito.verify(adapter, Mockito.atLeastOnce()).copyBodyToResponse(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        Runnable verifyAddListener = () -> {
            Mockito.verify(adapter, Mockito.atLeastOnce()).addListener(adapter, request, response);
        };

        return Stream.of(
                arguments(emptyMocker, verifyEmpty),
                arguments(mocker1, verifyEmpty),
                arguments(mocker2, verifyCopyBody),
                arguments(mocker3, verifyCopyBody),
                arguments(mocker4, verifyCopyBody),
                arguments(mocker5, verifyAddListener),
                arguments(mocker6, verifyEmpty),
                arguments(mockThrowException, verifyEmpty)
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
