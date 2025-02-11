package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.MockResult;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.match.strategy.AccurateMatchStrategy;
import io.arex.inst.runtime.util.IgnoreUtils;
import io.arex.inst.runtime.util.MockUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
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

    @ParameterizedTest
    @MethodSource("matchCase")
    void match(Runnable mocker, Mocker requestMocker, Predicate<Mocker> predicate) {
        mocker.run();
        Mocker result = ReplayMatcher.match(requestMocker, MockStrategyEnum.FIND_LAST);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> matchCase() {
        ArexMocker requestMocker = new ArexMocker(MockCategoryType.DYNAMIC_CLASS);
        requestMocker.setOperationName("mock");
        requestMocker.setTargetRequest(new Mocker.Target());
        requestMocker.getTargetRequest().setBody("mock");
        requestMocker.setTargetResponse(new Mocker.Target());

        ArexContext context = Mockito.mock(ArexContext.class);
        Map<Integer, List<Mocker>> cachedReplayResultMap = new HashMap<>();
        List<Mocker> mergeReplayList = new ArrayList<>();
        mergeReplayList.add(requestMocker);
        Runnable mockerContext = () -> {
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
            Mockito.when(context.getCachedReplayResultMap()).thenReturn(cachedReplayResultMap);
        };
        Runnable mockerStrategy = () -> {
            Mockito.when(MatchStrategyRegister.getMatchStrategies(any(), anyInt()))
                    .thenReturn(Collections.singletonList(new AccurateMatchStrategy()));
            Mockito.when(Config.get().isEnableDebug()).thenReturn(true);
            cachedReplayResultMap.put(1, mergeReplayList);
        };
        Runnable dynamicCLassRecordListMocker = () -> {
            cachedReplayResultMap.put(MatchKeyFactory.INSTANCE.getFuzzyMatchKey(requestMocker), mergeReplayList);
        };

        Runnable dataBaseRecordListMocker = () -> {
            requestMocker.setCategoryType(MockCategoryType.DATABASE);
            Mockito.when(context.getReplayCompareResultQueue()).thenReturn(new LinkedBlockingQueue<>());
            cachedReplayResultMap.put(MatchKeyFactory.INSTANCE.getFuzzyMatchKey(requestMocker), mergeReplayList);
        };

        Predicate<Mocker> predicate1 = Objects::isNull;
        Predicate<Mocker> predicate2 = Objects::nonNull;
        return Stream.of(
                arguments(mockerContext, requestMocker, predicate1),
                arguments(mockerStrategy, requestMocker, predicate1),
                arguments(dynamicCLassRecordListMocker, requestMocker, predicate2),
                arguments(dataBaseRecordListMocker, requestMocker, predicate2)
        );
    }
}
