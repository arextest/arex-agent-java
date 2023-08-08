package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.internal.Pair;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
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
import static org.mockito.ArgumentMatchers.anyString;
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

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("onServiceEnterCase")
    void onServiceEnter(String log, Runnable mocker, Predicate<Pair> predicate) {
        mocker.run();
        Pair result = ServletAdviceHelper.onServiceEnter(adapter, new Object(), new Object());
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> onServiceEnterCase() {
        Runnable emptyMocker = () -> {};
        Runnable main1 = () -> {
            Mockito.when(adapter.asHttpServletRequest(any())).thenReturn("mock");
            Mockito.when(adapter.markProcessed(any(), any())).thenReturn(true);
        };
        Runnable main2 = () -> {
            Mockito.when(adapter.markProcessed(any(), any())).thenReturn(false);
        };
        Runnable main3 = () -> {
            Mockito.when(adapter.asHttpServletResponse(any())).thenReturn("mock");
            Mockito.when(adapter.getAttribute(any(), any())).thenReturn(Boolean.TRUE);
        };
        Runnable shouldSkip1 = () -> {
            Mockito.when(adapter.getAttribute(any(), any())).thenReturn(Boolean.FALSE);
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.RECORD_ID))).thenReturn("mock");
        };
        Runnable getRedirectRecordId1 = () -> {
            Mockito.when(adapter.getParameter(any(), eq(ArexConstants.RECORD_ID))).thenReturn(null);
        };
        Runnable getRedirectRecordId2 = () -> {
            Mockito.when(adapter.getParameter(any(), eq(ArexConstants.RECORD_ID))).thenReturn("mock-redirectRecordId");
            Mockito.when(adapter.getRequestHeader(any(), eq("referer"))).thenReturn(null);
        };
        Runnable getRedirectRecordId3 = () -> {
            Mockito.when(adapter.getParameter(any(), eq(ArexConstants.RECORD_ID))).thenReturn("mock-redirectRecordId");
            Mockito.when(adapter.getRequestHeader(any(), eq("referer"))).thenReturn("mock-referer");
            ArexContext context = ArexContext.of("mock-record-id");
            context.setAttachment(ArexConstants.REDIRECT_REFERER, "mock-referer");
            Mockito.when(ContextManager.getRecordContext(anyString())).thenReturn(context);
        };
        Runnable shouldSkip2 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.RECORD_ID))).thenReturn("");
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.FORCE_RECORD))).thenReturn("true");
        };
        Runnable shouldSkip3 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.FORCE_RECORD))).thenReturn("false");
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.REPLAY_WARM_UP))).thenReturn("true");
        };
        Runnable shouldSkip4 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.REPLAY_WARM_UP))).thenReturn("false");
            Mockito.when(adapter.getRequestURI(any())).thenReturn("");
        };
        Runnable shouldSkip5 = () -> {
            Mockito.when(adapter.getRequestURI(any())).thenReturn("uri");
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(true);
        };
        Runnable shouldSkip6 = () -> {
            Mockito.when(IgnoreUtils.excludeEntranceOperation(any())).thenReturn(false);
            Mockito.when(adapter.getRequestURI(any())).thenReturn(".png");
        };
        Runnable shouldSkip7 = () -> {
            Mockito.when(adapter.getRequestURI(any())).thenReturn("uri");
            Mockito.when(adapter.getContentType(any())).thenReturn("image/");
        };
        Runnable shouldSkip8 = () -> {
            Mockito.when(adapter.getContentType(any())).thenReturn("mock");
            Mockito.when(RecordLimiter.acquire(any())).thenReturn(true);
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        };
        Runnable shouldSkip9 = () -> {
            Mockito.when(adapter.getRequestHeader(any(), eq(ArexConstants.RECORD_ID))).thenReturn("mock");
            Mockito.when(adapter.getAttribute(any(), eq(ArexConstants.SKIP_FLAG))).thenReturn(Boolean.TRUE);
        };

        Predicate<Pair<?, ?>> predicate1 = Objects::isNull;
        Predicate<Pair<?, ?>> predicate2 = Objects::nonNull;
        return Stream.of(
            arguments("adapter.httpServletRequest returns null", emptyMocker, predicate1),
            arguments("adapter.markProcessed returns true", main1, predicate1),
            arguments("adapter.asHttpServletResponse returns null", main2, predicate1),
            arguments("adapter.getAttribute returns true", main3, predicate1),
            arguments("shouldSkip: header: arex-record-id not null && config: arex.disable.replay is false", shouldSkip1, predicate1),
            arguments("getRedirectRecordId: parameter: arex-record-id is null", getRedirectRecordId1, predicate1),
            arguments("getRedirectRecordId: header: referer i null", getRedirectRecordId2, predicate1),
            arguments("getRedirectRecordId: header: getRedirectRecordId returns not null", getRedirectRecordId3, predicate1),
            arguments("shouldSkip: header: arex-force-record is true", shouldSkip2, predicate1),
            arguments("shouldSkip: arex-replay-warm-up is true", shouldSkip3, predicate1),
            arguments("shouldSkip: header: adapter.getRequestURI returns empty", shouldSkip4, predicate1),
            arguments("shouldSkip: IgnoreUtils.ignoreOperation returns true", shouldSkip5, predicate1),
            arguments("shouldSkip: adapter.getRequestURI return .png", shouldSkip6, predicate1),
            arguments("shouldSkip: adapter.getContentType return image/", shouldSkip7, predicate1),
            arguments("ContextManager.needRecordOrReplay is true", shouldSkip8, predicate2),
            arguments("shouldSkip: adapter.getAttribute returns true", shouldSkip9, predicate1)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("onServiceExitCase")
    void onServiceExit(String log, Runnable mocker, Runnable verify) {
        try (MockedConstruction<ServletExtractor> mocked = Mockito.mockConstruction(ServletExtractor.class, (mock, context) -> {
            System.out.println("mock ServletExtractor");
        })) {
            mocker.run();
            ServletAdviceHelper.onServiceExit(adapter, request, response);
            assertDoesNotThrow(verify::run);
        }
    }

    static Stream<Arguments> onServiceExitCase() {
        Runnable mockRequestOrResponseIsNUll = () -> {};
        Runnable mockWrapFalse = () -> {
            Mockito.when(adapter.asHttpServletRequest(any())).thenReturn(request);
            Mockito.when(adapter.asHttpServletResponse(any())).thenReturn(response);
            Mockito.when(adapter.wrapped(any(), any())).thenReturn(false);
        };

        Runnable mockRecordOrReplayIsFalse = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
            Mockito.when(adapter.wrapped(any(), any())).thenReturn(true);
        };
        Runnable mockRecordOrReplayIsTrue = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
        };
        Runnable mockAsyncFlagIsTrue = () -> {
            Mockito.when(adapter.getAttribute(any(), eq(ServletAdviceHelper.SERVLET_ASYNC_FLAG))).thenReturn(Boolean.TRUE);
        };
        Runnable mockAsyncStartedIsTrue = () -> {
            Mockito.when(adapter.getAttribute(any(), eq(ServletAdviceHelper.SERVLET_ASYNC_FLAG))).thenReturn(Boolean.FALSE);
            Mockito.when(adapter.isAsyncStarted(any())).thenReturn(true);
        };
        Runnable mockAsyncStartedIsFalse = () -> {
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
                arguments("httpServletRequest or httpServletResponse is null", mockRequestOrResponseIsNUll, verifyEmpty),
                arguments("adapter.wrapped returns false", mockWrapFalse, verifyEmpty),
                arguments("ContextManager.needRecordOrReplay return false", mockRecordOrReplayIsFalse, verifyCopyBody),
                arguments("ContextManager.needRecordOrReplay return true", mockRecordOrReplayIsTrue, verifyCopyBody),
                arguments("request.attr:arex-async-flag returns true", mockAsyncFlagIsTrue, verifyCopyBody),
                arguments("adapter.isAsyncStarted returns true", mockAsyncStartedIsTrue, verifyAddListener),
                arguments("adapter.isAsyncStarted returns false", mockAsyncStartedIsFalse, verifyEmpty),
                arguments("mock throw exception", mockThrowException, verifyEmpty)
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
