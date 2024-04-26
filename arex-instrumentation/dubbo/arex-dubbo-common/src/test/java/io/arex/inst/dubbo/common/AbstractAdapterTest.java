package io.arex.inst.dubbo.common;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.MockUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AbstractAdapterTest {
    static AbstractAdapter adapter;
    static MockedStatic<MockUtils> mockUtilsMocker;

    @BeforeAll
    static void setUp() {
        adapter = new DubboAdapterTest();
        Mockito.mockStatic(ContextManager.class);
        mockUtilsMocker = Mockito.mockStatic(MockUtils.class);
    }

    @AfterAll
    static void tearDown() {
        adapter = null;
        mockUtilsMocker = null;
        Mockito.clearAllCaches();
    }

    @Test
    void doExecute() {
        Mockito.when(ContextManager.needReplay()).thenReturn(true);
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetResponse(new Mocker.Target());
        adapter.doExecute("mock", mocker);
        mockUtilsMocker.verify(() -> MockUtils.replayMocker(any()), times(1));

        Mockito.when(ContextManager.needReplay()).thenReturn(false);
        adapter.doExecute("mock", mocker);
        mockUtilsMocker.verify(() -> MockUtils.recordMocker(any()), times(1));
    }

    @Test
    void normalizeResponse() {
        assertEquals("mock", adapter.normalizeResponse("mock", false));
        assertEquals("mock", adapter.normalizeResponse("mock", true));
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("key", "val");
        assertNotNull(adapter.normalizeResponse(responseMap, true));
    }

    @Test
    void testDoExecute() {
    }

    @Test
    void testNormalizeResponse() {
    }

    @Test
    void getOperationName() {
    }

    @Test
    void getServiceOperation() {
        assertNull(adapter.getServiceOperation());
    }

    @Test
    void getRequest() {
        String actualResult = adapter.getRequest();
        assertEquals("attachment: " + ArexConstants.ORIGINAL_REQUEST, actualResult);
    }

    @Test
    void getRequestParamType() {
    }

    @Test
    void getRecordRequestType() {
    }

    @Test
    void getProtocol() {
    }

    @Test
    void getExcludeMockTemplate() {
        assertEquals("attachment: " + ArexConstants.HEADER_EXCLUDE_MOCK, adapter.getExcludeMockTemplate());
    }

    @Test
    void getCaseId() {
    }

    @Test
    void forceRecord() {
        assertFalse(adapter.forceRecord());
    }

    @Test
    void replayWarmUp() {
        assertFalse(adapter.replayWarmUp());
    }

    @Test
    void getGeneric() {
        assertEquals("attachment: " + DubboConstants.KEY_GENERIC, adapter.getGeneric());
    }

    @Test
    void getConfigVersion() {
        assertEquals("attachment: " + ArexConstants.CONFIG_VERSION, adapter.getConfigVersion());
    }

    @Test
    void getRequestHeaders() {
        Map<String, String> actualResult = adapter.getRequestHeaders();
        assertEquals(3, actualResult.size());
    }

    @Test
    void getPath() {
        assertEquals("attachment: path", adapter.getPath());
    }

    @Test
    void getValByKey() {
        assertEquals("parameter: null", adapter.getValByKey("null"));
    }

    @Test
    void getAttachment() {
        assertEquals("attachment: key1", adapter.getAttachment("key1", ArexConstants.HEADER_X_PREFIX));

        assertNull(adapter.getAttachment("null", null));

        assertEquals("attachment: X-null", adapter.getAttachment("null", ArexConstants.HEADER_X_PREFIX));
    }

    @Test
    void getAttachments() {

    }

    @Test
    void getParameter() {
    }

    @Test
    void testGetAttachment() {
    }

    @Test
    void getServiceName() {
    }

    @Test
    void getArguments() {
    }

    @Test
    void getParameterTypes() {
    }
}
