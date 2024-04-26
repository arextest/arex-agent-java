package io.arex.agent.bootstrap.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class NumberUtilTest {

    @ParameterizedTest
    @CsvSource(value ={
            "null, 0",
            "1,    1",
            "a,    0"
    }, nullValues={"null"})
    void toInt(String source, int expect) {
        assertEquals(expect, NumberUtil.toInt(source));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"1", "a"})
    void parseLong(String input) {
        if ("1".equals(input)) {
            assertEquals(1L, NumberUtil.parseLong(input));
            return;
        }
        assertEquals(0L, NumberUtil.parseLong(input));
    }
}
