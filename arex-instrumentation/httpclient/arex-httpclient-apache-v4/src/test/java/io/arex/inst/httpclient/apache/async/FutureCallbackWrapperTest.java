package io.arex.inst.httpclient.apache.async;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.runtime.context.ContextManager;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class FutureCallbackWrapperTest {
    static FutureCallback delegate;
    static HttpClientExtractor<HttpRequest, MockResult> extractor;
    static FutureCallbackWrapper target;

    @BeforeAll
    public static void beforeClass() {
        delegate = Mockito.mock(FutureCallback.class);
        extractor = Mockito.mock(HttpClientExtractor.class);
        target = new FutureCallbackWrapper(extractor, delegate);
        Mockito.mockStatic(ContextManager.class);
    }

    @AfterAll
    static void tearDown() {
        delegate = null;
        extractor = null;
        target = null;
        Mockito.clearAllCaches();
    }

    @Test
    void completed() {
        HttpResponse response = Mockito.mock(HttpResponse.class);
        target.completed(response);
        verify(delegate).completed(response);
    }

    @Test
    void failed() {
        Exception ex = new NullPointerException();
        target.failed(ex);
        verify(delegate).failed(ex);
    }

    @Test
    void cancelled() {
        target.cancelled();
        verify(delegate, atLeastOnce()).cancelled();
    }

    @Test
    void testReplay() {
        Mockito.when(extractor.replay()).thenReturn(MockResult.success("mock"));
        MockResult result = target.replay();
        assertNotNull(result);
    }

    @Test
    void testReplayWithMockResult() {
        assertDoesNotThrow(() -> {
            target.replay(MockResult.success("mock")).get();
        });

        assertThrows(ExecutionException.class, () -> {
            target.replay(MockResult.success(new RuntimeException(""))).get();
        });
    }

    @ParameterizedTest
    @MethodSource("wrapTestCase")
    void wrap(FutureCallback<?> delegate, boolean skip, Predicate<FutureCallback<?>> predicate, HttpRequest httpRequest) {
        try (MockedConstruction<ApacheHttpClientAdapter> mocked = Mockito.mockConstruction(ApacheHttpClientAdapter.class, (mock, context) -> {
            Mockito.when(mock.skipRemoteStorageRequest()).thenReturn(skip);
        })) {
            FutureCallback<?> result = target.wrap(httpRequest, delegate);
            assertTrue(predicate.test(result));
        } catch (Exception ignore) {
            // ignore exception
        }
    }

    static Stream<Arguments> wrapTestCase() throws Exception {
        Predicate<FutureCallback<?>> nonNull = Objects::nonNull;
        Predicate<FutureCallback<?>> isNull = Objects::isNull;
        return Stream.of(
                arguments(new FutureCallbackWrapper<>(null), true, nonNull, null),
                arguments(delegate, true, isNull, null),
                arguments(delegate, false, nonNull, null)
        );
    }

    @Test
    void wrap() {
        FutureCallback<?> delegateCallback = Mockito.mock(FutureCallback.class);
        assertInstanceOf(FutureCallbackWrapper.class, FutureCallbackWrapper.wrap(delegateCallback));
    }
}
