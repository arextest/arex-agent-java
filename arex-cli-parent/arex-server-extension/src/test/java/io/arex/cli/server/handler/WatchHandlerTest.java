package io.arex.cli.server.handler;

import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.services.StorageService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class WatchHandlerTest {
    static WatchHandler target = null;
    static StorageService storageService;

    @BeforeAll
    static void setUp() {
        target = new WatchHandler();
        storageService = Mockito.mock(StorageService.class);
        StorageService.INSTANCE = storageService;
    }

    @AfterAll
    static void tearDown() {
        target = null;
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
        Runnable mocker2 = () -> Mockito.when(storageService.queryList(any())).thenReturn(Collections.singletonList(new DiffMocker()));

        Predicate<String> predicate1 = Objects::isNull;
        Predicate<String> predicate2 = Objects::nonNull;

        return Stream.of(
                arguments(null, mocker1, predicate1),
                arguments(null, mocker2, predicate2)
        );
    }
}