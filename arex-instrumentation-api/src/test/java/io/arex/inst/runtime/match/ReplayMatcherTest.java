package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ReplayMatcherTest {

    @BeforeAll
    static void setUp() {
        Mockito.mockStatic(MockUtils.class);
        Mockito.mockStatic(ContextManager.class);
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
        Mockito.mockStatic(MatchStrategyRegister.class);
    }

    @AfterAll
    static void tearDown() {
        Mockito.clearAllCaches();
    }

    @Test
    void match() {
        ArexMocker requestMocker = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        requestMocker.setOperationName("mock");
        requestMocker.setTargetRequest(new Mocker.Target());
        requestMocker.setTargetResponse(new Mocker.Target());
        ArexContext context = Mockito.mock(ArexContext.class);
        Mockito.when(ContextManager.currentContext()).thenReturn(context);
        Map<Integer, List<MergeDTO>> cachedReplayResultMap = new HashMap<>();
        Mockito.when(context.getCachedReplayResultMap()).thenReturn(cachedReplayResultMap);
        assertNull(ReplayMatcher.match(requestMocker, MockStrategyEnum.FIND_LAST));

        Mockito.when(MockUtils.methodRequestTypeHash(requestMocker)).thenReturn(1);
        List<MergeDTO> mergeReplayList = new ArrayList<>();
        MergeDTO mergeDTO = new MergeDTO();
        mergeReplayList.add(mergeDTO);
        cachedReplayResultMap.put(1, mergeReplayList);
        Mockito.when(MatchStrategyRegister.getMatchStrategies(any())).thenReturn(Collections.singletonList(new AccurateMatchStrategy()));
        Mockito.when(Config.get().isEnableDebug()).thenReturn(true);
        assertNull(ReplayMatcher.match(requestMocker, MockStrategyEnum.FIND_LAST));
    }
}