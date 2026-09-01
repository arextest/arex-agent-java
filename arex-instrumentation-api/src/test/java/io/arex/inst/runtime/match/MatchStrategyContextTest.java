package io.arex.inst.runtime.match;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchStrategyContextTest {
    static MatchStrategyContext context;

    @BeforeAll
    static void setUp() {
        context = new MatchStrategyContext(null, null);
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
    void getRecordList() {
        assertNull(context.getRecordList());
    }

    @Test
    void setRecordList() {
        assertDoesNotThrow(() -> context.setRecordList(null));
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

    @Test
    void getMatchStrategy() {
        assertNull(context.getMatchStrategy());
    }

    @Test
    void setMatchStrategy() {
        assertDoesNotThrow(() -> context.setMatchStrategy(null));
    }

    @Test
    void getReason() {
        assertNull(context.getReason());
    }

    @Test
    void setReason() {
        assertDoesNotThrow(() -> context.setReason(null));
    }
}