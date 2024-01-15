package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.model.MergeDTO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractMatchStrategyTest {

    static AbstractMatchStrategy target;
    @BeforeAll
    static void setUp() {
        target = new FuzzyMatchStrategy();
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    void match() {
        assertDoesNotThrow(() -> target.match(null));
        MatchStrategyContext context = new MatchStrategyContext(new ArexMocker(), null, null);
        assertDoesNotThrow(() -> target.match(context));
    }

    @Test
    void buildMatchedMocker() {
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetResponse(new Mocker.Target());
        mocker.setTargetRequest(new Mocker.Target());
        Mocker result = target.buildMatchedMocker(mocker, null);
        assertNull(result);
        result = target.buildMatchedMocker(mocker, new MergeDTO());
        assertNotNull(result);
    }
}