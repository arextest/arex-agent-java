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
import org.mockito.stubbing.OngoingStubbing;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

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
        Runnable mocker1 = () -> {
        };
        Runnable mocker2 = () -> StorageService.INSTANCE = storageService;
        Mocker servletMocker = MockService.createServlet("");
        servletMocker.getTargetRequest().setAttribute("Headers", new HashMap<>());
        Function<Class<? extends Mocker>, OngoingStubbing<List<Mocker>>> stub = clazz ->
                Mockito.when(storageService.queryList(any(clazz), anyInt()));
        Runnable mocker3 = () -> stub.apply(ArexMocker.class).thenReturn(Collections.singletonList(servletMocker));
        Runnable mocker4 = () -> {
            // ServletEntrance
            Mocker servletMocker1 = MockService.createServlet("errorCode: 0");
            servletMocker1.getTargetRequest().setAttribute("Headers", new HashMap<>());
            // servletMocker1.setResponse();
            Mocker servletMocker2 = MockService.createServlet("errorCode: 1");
            // servletMocker2.setResponse("errorCode: 1");
            stub.apply(Mocker.class)
                    .thenReturn(Collections.singletonList(servletMocker))
                    .thenReturn(Collections.singletonList(servletMocker1))
                    .thenReturn(Collections.singletonList(servletMocker2));
            // ServiceCall
            Mocker httpClientMocker1 = MockService.createHttpClient("POST");
            //  httpClientMocker1.setRequest("userName: Lucas");
            Mocker httpClientMocker2 = MockService.createHttpClient("GET");
            //  httpClientMocker2.setRequest("userName: Mason");
            stub.apply(Mocker.class)
                    .thenReturn(Collections.singletonList(httpClientMocker1))
                    .thenReturn(Collections.singletonList(httpClientMocker2));
            // Database
            Mocker databaseMocker1 = MockService.createDatabase("testdb");
            databaseMocker1.getTargetRequest().setBody(
                    "select * from MOCKER_INFO where userId = ?");
            databaseMocker1.getTargetRequest().setAttribute("parameters", "123");

            Mocker databaseMocker2 = MockService.createDatabase("testdb");
            databaseMocker2.getTargetRequest().setBody(
                    "select * from MOCKER_INFO where userId = ?");
            databaseMocker2.getTargetRequest().setAttribute("parameters", "124");
            stub.apply(Mocker.class)
                    .thenReturn(Collections.singletonList(databaseMocker1))
                    .thenReturn(Collections.singletonList(databaseMocker2));
            // redis
            Mocker redisMocker1 = MockService.createRedis("get1");
            Mocker redisMocker2 = MockService.createRedis("get2");

            stub.apply(Mocker.class)
                    .thenReturn(Collections.singletonList(redisMocker1))
                    .thenReturn(Collections.singletonList(redisMocker2));
            // dynamicClass
            Mocker dynamicClassMocker1 = MockService.createDynamicClass("com.test.Foo", "foo1");
            Mocker dynamicClassMocker2 = MockService.createDynamicClass("com.test.Foo", "foo2");
            stub.apply(Mocker.class)
                    .thenReturn(Collections.singletonList(dynamicClassMocker1))
                    .thenReturn(Collections.singletonList(dynamicClassMocker2));
        };

        Predicate<String> predicate1 = Objects::isNull;
      //  Predicate<String> predicate2 = result -> result.equals("[{\"diffCount\":5}]");

        return Stream.of(
                arguments("10", mocker1, predicate1),
                arguments("10", mocker2, predicate1),
                arguments("10", mocker3, predicate1),
                arguments("10", mocker4, predicate1)
        );
    }
}