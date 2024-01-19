package io.arex.inst.runtime.match;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EigenMatchStrategyTest {
    static EigenMatchStrategy eigenMatchStrategy;

    @BeforeAll
    static void setUp() {
        eigenMatchStrategy = new EigenMatchStrategy();
    }

    @AfterAll
    static void tearDown() {
        eigenMatchStrategy = null;
    }
}