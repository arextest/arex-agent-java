package io.arex.cli.server.handler;

import io.arex.foundation.model.*;
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
import org.mockito.stubbing.OngoingStubbing;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReplayHandlerTest {
    static ReplayHandler target = null;
    static StorageService storageService;

    @BeforeAll
    static void setUp() {
        target = new ReplayHandler();
        storageService = Mockito.mock(StorageService.class);
        Mockito.mockStatic(AsyncHttpClientUtil.class);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("responseBody", "test");
        Mockito.when(AsyncHttpClientUtil.executeAsyncIncludeHeader(anyString(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(responseMap));
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
        Runnable mocker1 = () -> {};
        Runnable mocker2 = () -> StorageService.INSTANCE = storageService;
        ServiceEntranceMocker servletMocker = new ServiceEntranceMocker();
        servletMocker.setRequestHeaders(new HashMap<>());
        Function<Class<? extends AbstractMocker>, OngoingStubbing<List<AbstractMocker>>> stub = clazz ->
                Mockito.when(storageService.queryList(any(clazz), anyInt()));
        Runnable mocker3 = () -> stub.apply(ServiceEntranceMocker.class).thenReturn(Collections.singletonList(servletMocker));
        Runnable mocker4 = () -> {
            // ServletEntrance
            ServiceEntranceMocker servletMocker1 = new ServiceEntranceMocker();
            servletMocker1.setResponse("errorCode: 0");
            ServiceEntranceMocker servletMocker2 = new ServiceEntranceMocker();
            servletMocker2.setResponse("errorCode: 1");
            stub.apply(ServiceEntranceMocker.class)
                    .thenReturn(Collections.singletonList(servletMocker))
                    .thenReturn(Collections.singletonList(servletMocker1))
                    .thenReturn(Collections.singletonList(servletMocker2));
            // ServiceCall
            HttpClientMocker httpClientMocker1 = new HttpClientMocker();
            httpClientMocker1.setRequest("userName: Lucas");
            HttpClientMocker httpClientMocker2 = new HttpClientMocker();
            httpClientMocker2.setRequest("userName: Mason");
            stub.apply(HttpClientMocker.class)
                    .thenReturn(Collections.singletonList(httpClientMocker1))
                    .thenReturn(Collections.singletonList(httpClientMocker2));
            // Database
            DatabaseMocker databaseMocker1 = new DatabaseMocker(
                    "testdb", "select * from MOCKER_INFO where userId = ?", "123");
            DatabaseMocker databaseMocker2 = new DatabaseMocker(
                    "testdb", "select * from MOCKER_INFO where userId = ?", "124");
            stub.apply(DatabaseMocker.class)
                    .thenReturn(Collections.singletonList(databaseMocker1))
                    .thenReturn(Collections.singletonList(databaseMocker2));
            // redis
            RedisMocker redisMocker1 = new RedisMocker(
                    "test_cluster", "userId_123", "get", "");
            RedisMocker redisMocker2 = new RedisMocker(
                    "test_cluster", "userId_124", "get", "");
            stub.apply(RedisMocker.class)
                    .thenReturn(Collections.singletonList(redisMocker1))
                    .thenReturn(Collections.singletonList(redisMocker2));
            // dynamicClass
            DynamicClassMocker dynamicClassMocker1 = new DynamicClassMocker(
                    "com.test.Foo", "foo", "apple");
            DynamicClassMocker dynamicClassMocker2 = new DynamicClassMocker(
                    "com.test.Foo", "foo", "banana");
            stub.apply(DynamicClassMocker.class)
                    .thenReturn(Collections.singletonList(dynamicClassMocker1))
                    .thenReturn(Collections.singletonList(dynamicClassMocker2));
        };

        Predicate<String> predicate1 = Objects::isNull;
        Predicate<String> predicate2 = result -> result.equals("[{\"diffCount\":5}]");

        return Stream.of(
                arguments("10", mocker1, predicate1),
                arguments("10", mocker2, predicate1),
                arguments("10", mocker3, predicate1),
                arguments("10", mocker4, predicate2)
        );
    }
}