package io.arex.inst.database.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class DatabaseExtractorTest {

    @InjectMocks
    DatabaseExtractor target;

    @Test
    void record() {
        try (MockedStatic<MockUtils> mockService = mockStatic(MockUtils.class);
             MockedStatic<Serializer> serializer = mockStatic(Serializer.class)) {
            ArexMocker mocker = new ArexMocker();
            mocker.setTargetRequest(new Target());
            mocker.setTargetResponse(new Target());
            mockService.when(() -> MockUtils.createDatabase(any())).thenReturn(mocker);
            mockService.when(() -> MockUtils.recordMocker(any())).then((Answer<Void>) invocationOnMock -> {
                System.out.println("mock MockService.recordMocker");
                return null;
            });

            Object object = new Object();
            target.recordDb(object);
            serializer.verify(() -> Serializer.serializeWithType(object), Mockito.never());
            // Map<String, Object>
            Map<String, Object> map = new HashMap<>();
            map.put("bigDecimal", new BigDecimal(1));
            target.recordDb(map);
            serializer.verify(() -> Serializer.serializeWithType(map), Mockito.times(1));
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

            ArexMocker replayMocker = new ArexMocker();
            replayMocker.setTargetRequest(new Target());
            Target response = new Target();
            response.setBody("body");
            response.setType("type");
            replayMocker.setTargetResponse(response);
            mockService.when(() -> MockUtils.createDatabase(any())).thenReturn(mocker);
            mockService.when(() -> MockUtils.replayMocker(any())).thenReturn(replayMocker);

            mockService.when(() -> MockUtils.checkResponseMocker(any())).thenReturn(true);

            serializer.when(() -> Serializer.deserialize(anyString(), anyString(), eq(null))).thenReturn(new Object());

            MockResult mockResult = MockResult.success(true, null);
            mockService.when(() -> MockUtils.replayBody(any())).thenReturn(mockResult);

            assertEquals(mockResult.isIgnoreMockResult(), target.replay().isIgnoreMockResult());

            // replay deserializeWithType
            response.setAttribute(ArexConstants.AREX_SERIALIZER, ArexConstants.JACKSON_SERIALIZER_WITH_TYPE);
            target.replay();
            serializer.verify(() -> Serializer.deserializeWithType(response.getBody()), Mockito.times(1));
        }
    }
}
