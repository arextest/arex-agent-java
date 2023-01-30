package io.arex.foundation.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdkUtilsTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getJavaVersion() {
        assertTrue(JdkUtils.getJavaVersion() > 0);
    }

    @Test
    void isJdk11() {
        assertInstanceOf(Boolean.class, JdkUtils.isJdk11());
    }
}