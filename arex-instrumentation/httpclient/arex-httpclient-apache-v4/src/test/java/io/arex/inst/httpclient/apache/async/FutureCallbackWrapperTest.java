package io.arex.inst.httpclient.apache.async;

import io.arex.agent.bootstrap.model.MockResult;
import io.arex.inst.httpclient.apache.common.ApacheHttpClientAdapter;
import io.arex.inst.httpclient.common.ExceptionWrapper;
import io.arex.inst.httpclient.common.HttpClientExtractor;
import io.arex.inst.httpclient.common.HttpResponseWrapper;
import io.arex.inst.runtime.context.ContextManager;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

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

    @ParameterizedTest
    @MethodSource("replayCase")
    void replay(Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        boolean result = target.replay();
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> replayCase() {
        HttpResponseWrapper wrapped = Mockito.mock(HttpResponseWrapper.class);
        Runnable mocker1 = () -> {};
        Runnable mocker2 = () -> {
            Mockito.when(wrapped.getException()).thenReturn(new ExceptionWrapper(null));
            Mockito.when(extractor.fetchMockResult()).thenReturn(wrapped);
        };
        Runnable mocker3 = () -> {
            Mockito.when(wrapped.getException()).thenReturn(new ExceptionWrapper(new NullPointerException()));
        };
        Runnable mocker4 = () -> {
            Mockito.when(wrapped.getException()).thenReturn(null);
            Mockito.when(extractor.replay(any())).thenReturn(MockResult.success("mock"));
        };

        Predicate<Boolean> predicate1 = result -> result;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate1),
                arguments(mocker3, predicate1),
                arguments(mocker4, predicate1)
        );
    }

    @ParameterizedTest
    @MethodSource("getCase")
    void get(FutureCallback delegate, boolean skip, Predicate<FutureCallbackWrapper> predicate) {
        try (MockedConstruction<ApacheHttpClientAdapter> mocked = Mockito.mockConstruction(ApacheHttpClientAdapter.class, (mock, context) -> {
            Mockito.when(mock.skipRemoteStorageRequest()).thenReturn(skip);
        })) {
            HttpAsyncRequestProducer requestProducer = Mockito.mock(HttpAsyncRequestProducer.class);
            FutureCallbackWrapper result = target.get(requestProducer, delegate);
            assertTrue(predicate.test(result));
        }
    }

    static Stream<Arguments> getCase() {
        FutureCallback delegate1 = Mockito.mock(FutureCallbackWrapper.class);

        Predicate<FutureCallbackWrapper> predicate1 = Objects::nonNull;
        Predicate<FutureCallbackWrapper> predicate2 = Objects::isNull;
        return Stream.of(
                arguments(delegate1, true, predicate1),
                arguments(delegate, true, predicate2),
                arguments(delegate, false, predicate1)
        );
    }
}