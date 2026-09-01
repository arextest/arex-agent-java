package io.arex.inst.runtime.match.key;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMatchKeyBuilderImplTest {

    static DefaultMatchKeyBuilderImpl instance;
    static ArexMocker mocker;

    @BeforeAll
    static void setUp() {
        instance = new DefaultMatchKeyBuilderImpl();
        mocker = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        mocker.setOperationName("mock");
        mocker.setTargetRequest(new Mocker.Target());
        mocker.getTargetRequest().setBody("mock");
        mocker.setTargetResponse(new Mocker.Target());
    }

    @AfterAll
    static void tearDown() {
        instance = null;
        mocker = null;
    }
    @Test
    void isSupported() {
        assertTrue(instance.isSupported(MockCategoryType.DYNAMIC_CLASS));
    }

    @Test
    void getFuzzyMatchKey() {
        assertNotEquals(instance.getFuzzyMatchKey(mocker), 0);
    }

    @Test
    void getAccurateMatchKey() {
        assertNotEquals(instance.getAccurateMatchKey(mocker), 0);
    }

    @Test
    void getEigenBody() {
        assertNotNull(instance.getEigenBody(mocker));
    }
}