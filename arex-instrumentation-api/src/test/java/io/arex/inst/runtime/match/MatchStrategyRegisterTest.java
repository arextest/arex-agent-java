package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchStrategyRegisterTest {

    @Test
    void getMatchStrategies() {
        assertNotNull(MatchStrategyRegister.getMatchStrategies(new ArexMocker(MockCategoryType.DYNAMIC_CLASS), 1));
        assertNotNull(MatchStrategyRegister.getMatchStrategies(new ArexMocker(MockCategoryType.DYNAMIC_CLASS), 2));
        assertNotNull(MatchStrategyRegister.getMatchStrategies(new ArexMocker(MockCategoryType.DATABASE), 2));
    }
}