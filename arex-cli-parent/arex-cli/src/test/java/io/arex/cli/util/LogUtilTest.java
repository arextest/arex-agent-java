package io.arex.cli.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class LogUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "{}"})
    void info(String from) {
        LogUtil.info(from, "test");
    }

    @Test
    void warn() {
        LogUtil.warn("warn{}", "test");
    }

    @Test
    void getLogDir() {
        assertNotNull(LogUtil.getLogDir());
    }
}