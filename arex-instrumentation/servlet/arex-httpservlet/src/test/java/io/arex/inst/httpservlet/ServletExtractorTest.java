package io.arex.inst.httpservlet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.httpservlet.adapter.ServletAdapter;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.MockUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

class ServletExtractorTest {

    static ServletAdapter<HttpServletRequest, HttpServletResponse> adapter;
    static HttpServletRequest request;
    static HttpServletResponse response;

    @BeforeAll
    static void setUp() {
        adapter = Mockito.mock(ServletAdapter.class);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(MockUtils.class);
    }

    @AfterAll
    static void tearDown() {
        adapter = null;
        request = null;
        response = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("executeCase")
    void execute(String log, Runnable mock, Runnable verify) throws IOException {
        mock.run();
        new ServletExtractor<>(adapter, request, response).execute();
        assertDoesNotThrow(verify::run);
    }


    static Stream<Arguments> executeCase() {
        Runnable mockResponseStatus302 = () -> {
            Mockito.when(adapter.getStatus(response)).thenReturn(302);
            Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock-trace-id"));
        };

        Runnable mockResponseStatus100 = () -> {
            Mockito.when(adapter.getStatus(response)).thenReturn(100);
        };

        Runnable mockResponseHeaderContainsReplayId = () -> {
            Mockito.when(adapter.getStatus(response)).thenReturn(200);
            Mockito.when(adapter.getResponseHeader(response, ArexConstants.REPLAY_ID)).thenReturn("mock-replay-id");
        };

        Runnable verifyCopyToResponse = () -> {
            try {
                Mockito.verify(adapter, Mockito.atLeastOnce()).copyBodyToResponse(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable mockNeedRecordOrReplayIsFalse = () -> {
            Mockito.when(adapter.getResponseHeader(response, ArexConstants.REPLAY_ID)).thenReturn(null);
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
        };

        Runnable mockNotRedirectRequest = () -> {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);

            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            Mockito.when(MockUtils.createServlet(any())).thenReturn(mocker);

            Mockito.when(adapter.getRequestHeaderNames(request)).thenReturn(Collections.enumeration(Arrays.asList("mock-header-name", "referer")));
            Mockito.when(adapter.getResponseHeaderNames(response)).thenReturn(Collections.singleton("mock-header-name"));
            Mockito.when(adapter.getRequestBytes(request)).thenReturn(new byte[0]);
            Mockito.when(adapter.getAttribute(request, ServletAdviceHelper.SERVLET_RESPONSE)).thenReturn(new Object());
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock-trace-id"));
        };
        Runnable mockRedirectRequest = () -> {
            ArexContext context = ArexContext.of("mock-trace-id");
            context.setRedirectRequest(true);
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
        };

        Runnable verifyResponseHeaderContainsTrace = () -> {
            Mockito.verify(adapter, Mockito.atLeastOnce()).setResponseHeader(response, ArexConstants.RECORD_ID, "mock-trace-id");
        };

        Runnable mockNeedRecord = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };

        Runnable verifySetResponseHeader = () -> {
            Mockito.verify(adapter).setResponseHeader(response, ArexConstants.REPLAY_ID, null);
        };

        Runnable mockRequestMethodIsGet = () -> {
            Mockito.when(adapter.getMethod(request)).thenReturn("GET");
            Mockito.when(adapter.getAttribute(request, ServletAdviceHelper.SERVLET_RESPONSE)).thenReturn(null);
        };

        Runnable verifyGetResponseBytes = () -> {
            Mockito.verify(adapter).getResponseBytes(response);
        };

        return Stream.of(
            arguments("response status is 302", mockResponseStatus302, verifyCopyToResponse),
            arguments("response status is 200", mockResponseStatus100, verifyCopyToResponse),
            arguments("response header contains arex trace", mockResponseHeaderContainsReplayId, verifyCopyToResponse),
            arguments("no need record or replay", mockNeedRecordOrReplayIsFalse, verifyCopyToResponse),
            arguments("record execute not redirect request", mockNotRedirectRequest, verifyResponseHeaderContainsTrace),
            arguments("record execute redirect request", mockRedirectRequest, verifyResponseHeaderContainsTrace),
            arguments("replay execute", mockNeedRecord, verifySetResponseHeader),
            arguments("get method", mockRequestMethodIsGet, verifyGetResponseBytes)
        );
    }
}
