package io.arex.inst.httpclient.apache.sync;

import io.arex.foundation.context.ContextManager;
import io.arex.foundation.context.RepeatedCollectManager;
import io.arex.foundation.model.MockResult;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
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

import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class InternalHttpClientInstrumentationTest {
    static InternalHttpClientInstrumentation target;

    @BeforeAll
    static void setUp() {
        target = new InternalHttpClientInstrumentation();
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(RepeatedCollectManager.class);
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
    void adviceClassNames() {
        assertNotNull(target.adviceClassNames());
    }

    @Test
    void onEnter() {
        try (MockedConstruction<HttpClientExtractor> mocked = Mockito.mockConstruction(HttpClientExtractor.class, (mock, context) -> {
            Mockito.when(mock.replay()).thenReturn(MockResult.of("mock"));
        })) {
            Mockito.when(ContextManager.needRecordOrReplay()).thenReturn(true);
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
            assertTrue(InternalHttpClientInstrumentation.ExecuteAdvice.onEnter(null, null, null));
        }
    }

    @ParameterizedTest
    @MethodSource("onExitCase")
    void onExit(HttpClientExtractor<HttpRequest, MockResult> extractor, Exception throwable,
                CloseableHttpResponse response, Predicate<CloseableHttpResponse> predicate) throws IOException {
        InternalHttpClientInstrumentation.ExecuteAdvice.onExit(extractor, throwable, response, MockResult.of(response));
        assertTrue(predicate.test(response));
    }

    static Stream<Arguments> onExitCase() {
        HttpClientExtractor<HttpRequest, MockResult> extractor1 = null;
        Exception throwable1 = null;
        Exception throwable2 = new NullPointerException();

        Mockito.when(RepeatedCollectManager.exitAndValidate()).thenReturn(true);
        HttpClientExtractor<HttpRequest, MockResult> extractor2 = Mockito.mock(HttpClientExtractor.class);

        Mockito.when(ContextManager.needRecord()).thenReturn(true);
        CloseableHttpResponse response1 = null;
        CloseableHttpResponse response2 = Mockito.mock(CloseableHttpResponse.class);

        Predicate<CloseableHttpResponse> predicate1 = Objects::isNull;
        Predicate<CloseableHttpResponse> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(extractor1, throwable1, response1, predicate1),
                arguments(extractor2, throwable1, response1, predicate1),
                arguments(extractor2, throwable1, response2, predicate2),
                arguments(extractor2, throwable2, response2, predicate2)
        );
    }
}