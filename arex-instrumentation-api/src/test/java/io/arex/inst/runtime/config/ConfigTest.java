package io.arex.inst.runtime.config;

import io.arex.inst.runtime.context.RecordLimiter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ConfigTest {

    @ParameterizedTest
    @MethodSource("invalidCase")
    void invalidRecord(Runnable mocker, Predicate<Boolean> predicate) {
        mocker.run();
        assertTrue(predicate.test(Config.get().invalidRecord("mock")));
    }

    static Stream<Arguments> invalidCase() {
        ConfigBuilder config = ConfigBuilder.create("mock");
        Runnable mocker1 = () -> {
            config.enableDebug(true).build();
        };
        Runnable mocker2 = () -> {
            config.enableDebug(false).build();
        };
        Runnable mocker3 = () -> {
            config.recordRate(1).build();
        };
        Runnable mocker4 = () -> {
            config.addProperty("arex.during.work", "true").build();;
            RecordLimiter.init(mock -> true);
        };

        Predicate<Boolean> predicate1 = result -> !result;
        Predicate<Boolean> predicate2 = result -> result;
        return Stream.of(
                arguments(mocker1, predicate1),
                arguments(mocker2, predicate2),
                arguments(mocker3, predicate2),
                arguments(mocker4, predicate1)
        );
    }
}