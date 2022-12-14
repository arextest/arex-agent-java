package io.arex.inst.database.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class DatabaseExtractorTest {

    @InjectMocks
    DatabaseExtractor target;

    @Test
    void record() {
        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class)) {
            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockService.when(() -> MockUtils.createDatabase(any())).thenReturn(mocker);
            mockService.when(() -> MockUtils.recordMocker(any())).then((Answer<Void>) invocationOnMock -> {
                System.out.println("mock MockService.recordMocker");
                return null;
            });

            target.record(new Object());
        }
    }

    @Test
    void replay() throws SQLException {
        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class);
            MockedStatic<IgnoreUtils> ignoreService = mockStatic(IgnoreUtils.class);
            MockedStatic<Serializer> serializer = mockStatic(Serializer.class)) {
            ignoreService.when(() -> IgnoreUtils.ignoreMockResult(any(), any())).thenReturn(true);

            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockService.when(() -> MockUtils.createDatabase(any())).thenReturn(mocker);
            mockService.when(() -> MockUtils.replayMocker(any())).thenReturn(mocker);

            mockService.when(() -> MockUtils.checkResponseMocker(any())).thenReturn(true);

            serializer.when(() -> Serializer.deserialize(anyString(), anyString())).thenReturn(new Object());

            MockResult mockResult = MockResult.success(true, null);
            mockService.when(() -> MockUtils.replayBody(any())).thenReturn(mockResult);

            assertEquals(mockResult.isIgnoreMockResult(), target.replay().isIgnoreMockResult());
        }
    }
}