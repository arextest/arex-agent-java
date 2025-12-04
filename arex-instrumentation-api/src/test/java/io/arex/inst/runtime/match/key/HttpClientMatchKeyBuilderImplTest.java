package io.arex.inst.runtime.match.key;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientMatchKeyBuilderImplTest {
    static HttpClientMatchKeyBuilderImpl instance;
    static ArexMocker mocker;

    @BeforeAll
    static void setUp() {
        instance = new HttpClientMatchKeyBuilderImpl();
        mocker = new ArexMocker(MockCategoryType.HTTP_CLIENT);
        mocker.setOperationName("http");
        mocker.setTargetRequest(new Mocker.Target());
        mocker.getTargetRequest().setBody("mock");
        mocker.setTargetResponse(new Mocker.Target());
        Mockito.mockStatic(Serializer.class);
    }

    @AfterAll
    static void tearDown() {
        instance = null;
        mocker = null;
        Mockito.clearAllCaches();
    }
    @Test
    void isSupported() {
        assertTrue(instance.isSupported(MockCategoryType.HTTP_CLIENT));
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
        mocker.getTargetRequest().setAttribute(ArexConstants.HTTP_QUERY_STRING, "mock");
        assertNull(instance.getEigenBody(mocker));
    }
}