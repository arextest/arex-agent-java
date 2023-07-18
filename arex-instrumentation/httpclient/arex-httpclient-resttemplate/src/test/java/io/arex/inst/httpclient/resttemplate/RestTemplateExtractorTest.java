package io.arex.inst.httpclient.resttemplate;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.Mocker.Target;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
import java.net.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;

class RestTemplateExtractorTest {
    private static RestTemplateExtractor extractor = null;
    private  static MockedStatic<MockUtils> mockUtilsMockedStatic = null;

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(Serializer.class);
        mockUtilsMockedStatic = Mockito.mockStatic(MockUtils.class);
        final RequestCallback requestCallback = Mockito.mock(RequestCallback.class);
        final URI uri = Mockito.mock(URI.class);
        Mockito.when(uri.getPath()).thenReturn("mockPath");
        extractor = new RestTemplateExtractor(uri, HttpMethod.POST, requestCallback);
    }

    @AfterAll
    static void tearDown() {
        extractor = null;
        Mockito.clearAllCaches();
    }


    @Test
    void record() {
        try (MockedStatic<IgnoreUtils> ignoreUtilsMockedStatic = Mockito.mockStatic(IgnoreUtils.class)) {
            // exclude operation
            ignoreUtilsMockedStatic.when(() -> IgnoreUtils.excludeOperation("mockPath")).thenReturn(true);
            extractor.record("mockResponse", null);
            mockUtilsMockedStatic.verify(() -> MockUtils.recordMocker(Mockito.any()), Mockito.never());

            // not exclude operation
            ignoreUtilsMockedStatic.when(() -> IgnoreUtils.excludeOperation("mockPath")).thenReturn(false);
            final RuntimeException runtimeException = new RuntimeException();
            final ArexMocker mocker = makeMocker();
            mockUtilsMockedStatic.when(() -> MockUtils.createHttpClient("mockPath")).thenReturn(mocker);

            // record throwable
            extractor.record("mockResponse", runtimeException);
            assertEquals(mocker.getTargetResponse().getType(), TypeUtil.getName(runtimeException));

            // record normal response
            extractor.record("mockResponse", null);
            assertEquals(mocker.getTargetResponse().getType(), TypeUtil.getName("mockResponse"));

            // record null response
            extractor.record(null, null);
            assertNull(mocker.getTargetResponse().getType());

            // record response is ResponseEntity and body is null
            ResponseEntity<Object> nullBodyEntity = new ResponseEntity<>(HttpStatus.OK);
            extractor.record(nullBodyEntity, null);
            assertEquals(mocker.getTargetResponse().getType(), ResponseEntityWrapper.class.getName());

            // record response is ResponseEntity and body is not null
            ResponseEntity<String> notNullBodyEntity = new ResponseEntity<>("mockResponse", HttpStatus.OK);
            extractor.record(notNullBodyEntity, null);
            assertEquals(mocker.getTargetResponse().getType(), ResponseEntityWrapper.class.getName() + "-" + TypeUtil.getName("mockResponse"));

            // error
            Mockito.when(IgnoreUtils.excludeOperation("mockPath")).thenThrow(new RuntimeException());
            assertDoesNotThrow(() -> extractor.record("mockResponse", null));
        }
    }

    @Test
    void replay() {
        try (MockedStatic<IgnoreUtils> ignoreUtilsMockedStatic = Mockito.mockStatic(IgnoreUtils.class)) {
            // exclude operation
            ignoreUtilsMockedStatic.when(() -> IgnoreUtils.excludeOperation("mockPath")).thenReturn(true);
            extractor.replay();
            mockUtilsMockedStatic.verify(() -> MockUtils.replayMocker(Mockito.any()), Mockito.never());

            // not exclude operation
            ignoreUtilsMockedStatic.when(() -> IgnoreUtils.excludeOperation("mockPath")).thenReturn(false);
            final ArexMocker mocker = makeMocker();
            ArexMocker responseMocker = makeMocker();
            mockUtilsMockedStatic.when(() -> MockUtils.createHttpClient("mockPath")).thenReturn(mocker);

            // null response
            mockUtilsMockedStatic.when(() -> MockUtils.replayMocker(mocker)).thenReturn(responseMocker);
            assertNull(extractor.replay());

            // normal response
            mockUtilsMockedStatic.when(() -> MockUtils.checkResponseMocker(Mockito.any())).thenReturn(true);
            responseMocker.getTargetResponse().setType(TypeUtil.getName("mockResponse"));
            responseMocker.getTargetResponse().setBody("mockResponse");
            mockUtilsMockedStatic.when(() -> MockUtils.replayMocker(mocker)).thenReturn(responseMocker);
            Mockito.when(Serializer.deserialize("mockResponse", TypeUtil.forName("java.lang.String"), "gson")).thenReturn("mockResponse");
            assertEquals(extractor.replay().getResult(), "mockResponse");

            // responseWrapper
            responseMocker.getTargetResponse().setType(ResponseEntityWrapper.class.getName() + "-java.lang.String");
            responseMocker.getTargetResponse().setBody("mockResponseEntityWrapper");
            final ResponseEntityWrapper responseEntityWrapper = new ResponseEntityWrapper();
            responseEntityWrapper.setHttpStatus(200);
            responseEntityWrapper.setBody("mockResponse");
            mockUtilsMockedStatic.when(() -> MockUtils.replayMocker(mocker)).thenReturn(responseMocker);
            Mockito.when(Serializer.deserialize("mockResponseEntityWrapper",
                    TypeUtil.forName(ResponseEntityWrapper.class.getName() + "-java.lang.String"), "gson")).thenReturn(responseEntityWrapper);
            final MockResult mockResult = extractor.replay();
            assertTrue(mockResult.getResult() instanceof ResponseEntity);

            // error
            Mockito.when(IgnoreUtils.excludeOperation("mockPath")).thenThrow(new RuntimeException());
            assertDoesNotThrow(() -> extractor.replay());
        }
    }

    private ArexMocker makeMocker() {
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetRequest(new Target());
        mocker.setTargetResponse(new Target());
        return mocker;
    }
}