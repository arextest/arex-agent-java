package io.arex.cli.server.handler;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.foundation.services.MockService;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.AsyncHttpClientUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class DebugHandlerTest {
    static DebugHandler target = null;
    static StorageService storageService;

    @BeforeAll
    static void setUp() {
        target = new DebugHandler();
        storageService = Mockito.mock(StorageService.class);
        StorageService.INSTANCE = storageService;
        Mockito.mockStatic(AsyncHttpClientUtil.class);
    }

    @AfterAll
    static void tearDown() {
        target = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("processCase")
    void process(String command, Runnable mocker, Predicate<String> predicate) throws Exception {
        mocker.run();
        String result = target.process(command);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> processCase() {
        Runnable mocker1 = () -> {
        };
        ArexMocker servletMocker = MockService.createServlet("SERVLET");

        servletMocker.getTargetRequest().setAttribute("Headers", new HashMap<>());
        // servletMocker.setRequest("{}");
        Runnable mockerQuery =
                () -> Mockito.when(storageService.query(any(Mocker.class))).thenReturn(servletMocker);
        Runnable mocker2 = () -> {
            mockerQuery.run();
            Mockito.when(AsyncHttpClientUtil.executeAsyncIncludeHeader(anyString(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(null));
        };
        Runnable mocker3 = () -> {
            mockerQuery.run();
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("responseBody", "test debug");
            Mockito.when(AsyncHttpClientUtil.executeAsyncIncludeHeader(anyString(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(responseMap));
        };

        Predicate<String> predicate1 = result -> result.equals("query no result.");
        Predicate<String> predicate2 = result -> result.equals("response is null.");
        Predicate<String> predicate3 = result -> result.equals("test debug");

        return Stream.of(
                arguments(null, mocker1, predicate1),
                arguments(null, mocker2, predicate2),
                arguments(null, mocker3, predicate3)
        );
    }
}