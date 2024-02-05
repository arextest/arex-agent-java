package io.arex.inst.runtime.match;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchStrategyContextTest {
    static MatchStrategyContext context;

    @BeforeAll
    static void setUp() {
        context = new MatchStrategyContext(null, null, null);
    }

    @Test
    void getRequestMocker() {
        assertNull(context.getRequestMocker());
    }

    @Test
    void setRequestMocker() {
        assertDoesNotThrow(() -> context.setRequestMocker(null));
    }

    @Test
    void getMergeReplayList() {
        assertNull(context.getMergeReplayList());
    }

    @Test
    void setMergeReplayList() {
        assertDoesNotThrow(() -> context.setMergeReplayList(null));
    }

    @Test
    void getMockStrategy() {
        assertNull(context.getMockStrategy());
    }

    @Test
    void setMockStrategy() {
        assertDoesNotThrow(() -> context.setMockStrategy(null));
    }

    @Test
    void isInterrupt() {
        assertFalse(context.isInterrupt());
    }

    @Test
    void setInterrupt() {
        assertDoesNotThrow(() -> context.setInterrupt(false));
    }

    @Test
    void getMatchMocker() {
        assertNull(context.getMatchMocker());
    }

    @Test
    void setMatchMocker() {
        assertDoesNotThrow(() -> context.setMatchMocker(null));
    }
}