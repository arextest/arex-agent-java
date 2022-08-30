package io.arex.cli.server.handler;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RecordHandlerTest {
    static RecordHandler target = null;

    @BeforeAll
    static void setUp() {
        target = new RecordHandler();
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @ParameterizedTest
    @MethodSource("processCase")
    void process(String command, Predicate<String> predicate) throws Exception {
        String result = target.process(command);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> processCase() {
        Predicate<String> predicate1 = result -> result.startsWith("turn off record.");
        Predicate<String> predicate2 = result -> result.equals("invalid option.");
        Predicate<String> predicate3 = result -> result.equals("startup record");

        return Stream.of(
                arguments("-c-r=1", predicate1),
                arguments("d", predicate2),
                arguments(null, predicate3)
        );
    }
}