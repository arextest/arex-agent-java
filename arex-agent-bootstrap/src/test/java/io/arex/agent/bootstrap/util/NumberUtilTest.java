package io.arex.agent.bootstrap.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
}