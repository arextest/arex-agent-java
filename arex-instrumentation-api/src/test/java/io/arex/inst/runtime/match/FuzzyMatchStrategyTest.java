package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.match.strategy.FuzzyMatchStrategy;
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
        ArexMocker mocker = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        mocker.setOperationName("mock");
        mocker.setTargetRequest(new Mocker.Target());
        mocker.getTargetRequest().setBody("mock");
        mocker.setTargetResponse(new Mocker.Target());
        mocker.setAccurateMatchKey(MatchKeyFactory.INSTANCE.getAccurateMatchKey(mocker));
        List<Mocker> mergeReplayList = new ArrayList<>();
        mergeReplayList.add(mocker);
        MatchStrategyContext context = new MatchStrategyContext(mocker, MockStrategyEnum.FIND_LAST);
        context.setRecordList(mergeReplayList);
        Mockito.when(Config.get().isEnableDebug()).thenReturn(true);
        fuzzyMatchStrategy.process(context);
        assertNotNull(context.getMatchMocker());

        mocker.setMatched(true);
        fuzzyMatchStrategy.process(context);
        assertNotNull(context.getMatchMocker());
    }

    @Test
    void internalCheck() {
        assertFalse(fuzzyMatchStrategy.internalCheck(new MatchStrategyContext(null, null)));
    }
}