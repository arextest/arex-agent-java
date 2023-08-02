package io.arex.inst.authentication.jwt;

import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.jsonwebtoken.Jwt;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

class JJWTGeneratorTest {
    private static String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJiMDE1MzdkMi05NDc3LTRlZTItOWI3NC0zMGNjMDYzOTU3YzIiLCJzdWI" +
            "iOiJsdWNhcyIsImlhdCI6MTY5MDg3MTExNSwiZXhwIjoxNjkwODcxMTI1fQ.H5vbIl1hcjvbzJs08IlUPXV9gsMQF_mSMFAXP9J8ues";

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(Serializer.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("generateCase")
    void generate(Runnable mocker, String jwt, Predicate<Jwt> predicate) {
        mocker.run();
        Jwt result = JJWTGenerator.generate(jwt);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> generateCase() {
        Runnable emptyMocker = () -> {};
        Runnable mocker1 = () -> {
            Mockito.when(ContextManager.needReplay()).thenReturn(true);
        };
        Runnable mocker2 = () -> {
            Mockito.when(Serializer.deserialize(any(String.class), any(Class.class))).thenReturn(new HashMap<>());
        };

        Predicate<Jwt> predicate_isNull = Objects::isNull;
        Predicate<Jwt> predicate_nonNull = Objects::nonNull;
        return Stream.of(
                arguments(emptyMocker, "", predicate_isNull),
                arguments(mocker1, "", predicate_isNull),
                arguments(mocker1, token, predicate_isNull),
                arguments(mocker2, token, predicate_nonNull)
        );
    }
}