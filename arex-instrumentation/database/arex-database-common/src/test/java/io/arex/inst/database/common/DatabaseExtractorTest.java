package io.arex.inst.database.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.foundation.services.IgnoreService;
import io.arex.foundation.services.MockService;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseExtractorTest {

    @InjectMocks
    DatabaseExtractor target;

    @Test
    void record() {
        target.record(new Object());
    }

    @Test
    void testRecord() {
        target.record(new SQLException());
    }

    @Test
    void replay() throws SQLException {
        try (MockedStatic<MockService> mockService = mockStatic(MockService.class);
            MockedStatic<IgnoreService> ignoreService = mockStatic(IgnoreService.class)) {
            ignoreService.when(() -> IgnoreService.ignoreMockResult(any(), any())).thenReturn(true);
            MockResult mockResult = MockResult.success(true, null);
            mockService.when(() -> MockService.replayBody(any())).thenReturn(mockResult);

            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockService.when(() -> MockService.createDatabase(any())).thenReturn(mocker);

            assertEquals(mockResult.isIgnoreMockResult(), target.replay().isIgnoreMockResult());
        }
    }
}