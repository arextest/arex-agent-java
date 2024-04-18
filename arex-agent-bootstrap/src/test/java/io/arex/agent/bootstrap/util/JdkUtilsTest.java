package io.arex.agent.bootstrap.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        assertInstanceOf(Boolean.class, JdkUtils.isJdk11OrHigher());
    }

}
