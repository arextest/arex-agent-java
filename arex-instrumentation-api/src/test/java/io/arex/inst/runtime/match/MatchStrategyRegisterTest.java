package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchStrategyRegisterTest {

    @Test
    void getMatchStrategies() {
        assertNotNull(MatchStrategyRegister.getMatchStrategies(new ArexMocker(MockCategoryType.DYNAMIC_CLASS)));
    }
}