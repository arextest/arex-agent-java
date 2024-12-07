package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchKeyFactoryTest {

    static ArexMocker mocker;

    @BeforeAll
    static void setUp() {
        mocker = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        mocker.setOperationName("mock");
        mocker.setTargetRequest(new Mocker.Target());
        mocker.getTargetRequest().setBody("mock");
        mocker.setTargetResponse(new Mocker.Target());
    }

    @AfterAll
    static void tearDown() {
        mocker = null;
    }

    @Test
    void getFuzzyMatchKey() {
        assertNotEquals(MatchKeyFactory.INSTANCE.getFuzzyMatchKey(mocker), 0);
    }

    @Test
    void getAccurateMatchKey() {
        assertNotEquals(MatchKeyFactory.INSTANCE.getAccurateMatchKey(mocker), 0);
    }

    @Test
    void getEigenBody() {
        assertNotNull(MatchKeyFactory.INSTANCE.getEigenBody(mocker));
    }
}