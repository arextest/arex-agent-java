package io.arex.inst.runtime.match;

import io.arex.inst.runtime.match.strategy.EigenMatchStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

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