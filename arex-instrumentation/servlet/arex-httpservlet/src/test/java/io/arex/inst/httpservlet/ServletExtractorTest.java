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
        Runnable mock1 = () -> Mockito.when(adapter.getResponseHeader(response, ArexConstants.REPLAY_ID)).thenReturn("mock-replay-id");

        Runnable verify1 = () -> {
            try {
                Mockito.verify(adapter).copyBodyToResponse(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        Runnable mock2 = () -> {
            Mockito.when(adapter.getResponseHeader(response, ArexConstants.REPLAY_ID)).thenReturn(null);
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(false);
        };

        Runnable verify2 = () -> {};

        Runnable mock3 = () -> {
            Mockito.when(adapter.getResponseHeader(response, ArexConstants.REPLAY_ID)).thenReturn(null);
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);

            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            Mockito.when(MockUtils.createServlet(any())).thenReturn(mocker);

            Mockito.when(adapter.getRequestHeaderNames(request)).thenReturn(Collections.enumeration(Collections.singleton("mock-header-name")));
            Mockito.when(adapter.getResponseHeaderNames(response)).thenReturn(Collections.singleton("mock-header-name"));
            Mockito.when(adapter.getRequestBytes(request)).thenReturn(new byte[0]);
            Mockito.when(adapter.getAttribute(request, ServletAdviceHelper.SERVLET_RESPONSE)).thenReturn(new Object());
            Mockito.when(ContextManager.needRecord()).thenReturn(true);
            Mockito.when(ContextManager.currentContext()).thenReturn(ArexContext.of("mock-trace-id"));
        };
        Runnable verify3 = () -> {
            Mockito.verify(adapter).setResponseHeader(response, ArexConstants.RECORD_ID, "mock-trace-id");
        };

        Runnable mock4 = () -> {
            Mockito.when(ContextManager.needRecord()).thenReturn(false);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };

        Runnable verify4 = () -> {
            Mockito.verify(adapter).setResponseHeader(response, ArexConstants.REPLAY_ID, null);
        };

        Runnable mock5 = () -> {
            Mockito.when(adapter.getMethod(request)).thenReturn("GET");
            Mockito.when(adapter.getAttribute(request, ServletAdviceHelper.SERVLET_RESPONSE)).thenReturn(null);
        };

        Runnable verify5 = () -> {
            Mockito.verify(adapter).getResponseBytes(response);
        };

        return Stream.of(
            arguments("response header contains arex trace", mock1, verify1),
            arguments("no need record or replay", mock2, verify2),
            arguments("record execute", mock3, verify3),
            arguments("replay execute", mock4, verify4),
            arguments("get method", mock5, verify5)
        );
    }
}
