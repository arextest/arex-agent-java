package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.match.strategy.AbstractMatchStrategy;
import io.arex.inst.runtime.match.strategy.AccurateMatchStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AbstractMatchStrategyTest {
    static AbstractMatchStrategy target;
    static ArexMocker mocker;

    @BeforeAll
    static void setUp() {
        target = new AccurateMatchStrategy();
        mocker = new ArexMocker();
        mocker.setTargetResponse(new Mocker.Target());
        mocker.setTargetRequest(new Mocker.Target());
        mocker.getTargetRequest().setBody("mock");
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void match() {
        assertDoesNotThrow(() -> target.match(null));
        MatchStrategyContext context = new MatchStrategyContext(mocker, new ArrayList<>(), null);
        assertDoesNotThrow(() -> target.match(context));
    }
}