package io.arex.inst.httpclient.apache.sync;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.context.RepeatedCollectManager;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.util.IgnoreUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InternalHttpClientInstrumentationTest {
    static InternalHttpClientInstrumentation target;

    @BeforeAll
    static void setUp() {
        target = new InternalHttpClientInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
        Mockito.mockStatic(IgnoreUtils.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void typeMatcher() {
        assertNotNull(target.typeMatcher());
    }

    @Test
    void methodAdvices() {
        assertNotNull(target.methodAdvices());
    }

    @Test
    void onEnter() {
        assertFalse(InternalHttpClientInstrumentation.ExecuteAdvice.onEnter(Mockito.mock(HttpRequest.class), null, null));

        try (MockedConstruction<HttpClientExtractor> mocked = Mockito.mockConstruction(HttpClientExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.success("mock"));
        })) {
            Mockito.when(IgnoreUtils.ignoreOperation(any())).thenReturn(false);
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            assertTrue(InternalHttpClientInstrumentation.ExecuteAdvice.onEnter(new HttpPost("localhost"), null, null));
        }
    }

    @ParameterizedTest
    @MethodSource("onExitCase")
    void onExit(HttpClientExtractor<HttpRequest, HttpResponse> extractor, Exception throwable,
                CloseableHttpResponse response, Predicate<CloseableHttpResponse> predicate, MockResult mockResult) {
        InternalHttpClientInstrumentation.ExecuteAdvice.onExit(throwable, response, extractor, mockResult);
        assertTrue(predicate.test(response));
    }

    static Stream<Arguments> onExitCase() {
        Exception throwable2 = new NullPointerException();

        Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        HttpClientExtractor<HttpRequest, HttpResponse> extractor2 = Mockito.mock(HttpClientExtractor.class);

        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        CloseableHttpResponse response2 = Mockito.mock(CloseableHttpResponse.class);

        Predicate<CloseableHttpResponse> predicate1 = Objects::isNull;
        Predicate<CloseableHttpResponse> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(null, null, null, predicate1, MockResult.success(null)),
                arguments(extractor2, null, null, predicate1, MockResult.success(null)),
                arguments(extractor2, null, null, predicate1, MockResult.success(new RuntimeException("mock exception"))),
                arguments(extractor2, null, response2, predicate2, MockResult.success(response2)),
                arguments(extractor2, throwable2, response2, predicate2, MockResult.success(response2))
        );
    }
}