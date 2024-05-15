package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.util.MockUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuzzyMatchStrategyTest {
    static FuzzyMatchStrategy fuzzyMatchStrategy;

    @BeforeAll
    static void setUp() {
        fuzzyMatchStrategy = new FuzzyMatchStrategy();
        Mockito.mockStatic(Config.class);
        Mockito.when(Config.get()).thenReturn(Mockito.mock(Config.class));
        Mockito.mockStatic(MockUtils.class);
    }

    @AfterAll
    static void tearDown() {
        fuzzyMatchStrategy = null;
        Mockito.clearAllCaches();
    }

    @Test
    void process() {
        ArexMocker mocker = new ArexMocker();
        mocker.setTargetResponse(new Mocker.Target());
        mocker.setTargetRequest(new Mocker.Target());
        mocker.setCategoryType(MockCategoryType.DYNAMIC_CLASS);
        List<Mocker> mergeReplayList = new ArrayList<>();
        Mocker mergeDTO = new ArexMocker();
        mergeReplayList.add(mergeDTO);
        MatchStrategyContext context =new MatchStrategyContext(mocker, mergeReplayList, MockStrategyEnum.FIND_LAST);
        Mockito.when(Config.get().isEnableDebug()).thenReturn(true);
        fuzzyMatchStrategy.process(context);
        assertNotNull(context.getMatchMocker());

        mergeDTO.setMatched(true);
        fuzzyMatchStrategy.process(context);
        assertNotNull(context.getMatchMocker());
    }

    @Test
    void internalCheck() {
        assertFalse(fuzzyMatchStrategy.internalCheck(new MatchStrategyContext(null, null, null)));
    }
}