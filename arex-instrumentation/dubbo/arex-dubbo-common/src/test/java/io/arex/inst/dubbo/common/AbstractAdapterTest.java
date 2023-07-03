package io.arex.inst.dubbo.common;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.util.MockUtils;
import io.arex.inst.runtime.util.TypeUtil;
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
    void parseRequest() {
        Object[] requests = new Object[]{"mock"};
        assertEquals("[\"mock\"]", AbstractAdapter.parseRequest(requests, req -> ArrayUtils.toString(requests, null)));
        assertNotNull(AbstractAdapter.parseRequest(new Object[]{}, TypeUtil::getName));
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
}