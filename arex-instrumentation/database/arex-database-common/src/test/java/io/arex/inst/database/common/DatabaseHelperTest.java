package io.arex.inst.database.common;

import io.arex.inst.runtime.serializer.Serializer;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.TypedValue;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DatabaseHelperTest {
    @ParameterizedTest
    @MethodSource("parseParameterCase")
    void parseParameter(QueryParameters queryParameters, Predicate<String> predicate) {
        String result = DatabaseHelper.parseParameter(queryParameters);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> parseParameterCase() {
        QueryParameters queryParameters1 = Mockito.mock(QueryParameters.class);
        QueryParameters queryParameters2 = Mockito.mock(QueryParameters.class);

        Map<String, TypedValue> parameters = new HashMap<>();
        parameters.put("key", Mockito.mock(TypedValue.class));
        Mockito.when(queryParameters2.getNamedParameters()).thenReturn(parameters);

        Mockito.mockStatic(Serializer.class);
        Mockito.when(Serializer.serialize(any())).thenReturn("mock Serializer.serialize");

        Predicate<String> predicate1 = Objects::isNull;
        Predicate<String> predicate2 = Objects::nonNull;

        return Stream.of(
                arguments(null, predicate1),
                arguments(queryParameters1, predicate1),
                arguments(queryParameters2, predicate2)
        );
    }
}