package io.arex.inst.config.apollo;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.util.MockUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ApolloConfigExtractorTest {

    static MockedStatic<MockUtils> mockUtilsMocker;

    @BeforeAll
    static void setUp() {
        mockUtilsMocker = Mockito.mockStatic(MockUtils.class);
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetRequest(new Mocker.Target());
        mocker.setTargetResponse(new Mocker.Target());
        Mockito.when(MockUtils.createConfigFile(any())).thenReturn(mocker);
    }

    @AfterAll
    static void tearDown() {
        mockUtilsMocker = null;
        Mockito.clearAllCaches();
    }

    @Test
    void tryCreateExtractor() {
        assertNotNull(ApolloConfigExtractor.tryCreateExtractor());
        assertNull(ApolloConfigExtractor.tryCreateExtractor());
    }

    @Test
    void record() {
        ApolloConfigExtractor extractor = new ApolloConfigExtractor();
        extractor.record("mock", new Properties());
        mockUtilsMocker.verify(() -> MockUtils.recordMocker(any()), times(1));
    }

    @Test
    void duringReplay() {
        assertDoesNotThrow(ApolloConfigExtractor::duringReplay);
    }

    @Test
    void needRecord() {
        assertDoesNotThrow(ApolloConfigExtractor::needRecord);
    }

    @Test
    void onConfigUpdate() {
        ApolloConfigExtractor.onConfigUpdate();
        assertEquals(StringUtil.EMPTY, ApolloConfigExtractor.currentReplayConfigBatchNo());
    }

    @Test
    void updateReplayState() {
        ApolloConfigExtractor.updateReplayState("mock", "mock");
        assertEquals("mock", ApolloConfigExtractor.currentReplayConfigBatchNo());
    }

    @Test
    void replay() {
        ApolloConfigExtractor.replay("mock");
        mockUtilsMocker.verify(() -> MockUtils.replayBody(any()), times(1));
    }

    @Test
    void currentReplayConfigBatchNo() {
        assertDoesNotThrow(ApolloConfigExtractor::currentReplayConfigBatchNo);
    }

}