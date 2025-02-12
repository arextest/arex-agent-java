package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.match.strategy.EigenMatchStrategy;
import io.arex.inst.runtime.model.CompareConfigurationEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class EigenMatchStrategyTest {
    static EigenMatchStrategy eigenMatchStrategy;
    static Config config;

    @BeforeAll
    static void setUp() {
        eigenMatchStrategy = new EigenMatchStrategy();
        Mockito.mockStatic(Config.class);
        config = Mockito.mock(Config.class);
        Mockito.when(Config.get()).thenReturn(config);
    }

    @AfterAll
    static void tearDown() {
        eigenMatchStrategy = null;
        config = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("processCase")
    void process(Runnable mocker, MatchStrategyContext context, Predicate<MatchStrategyContext> asserts) {
        mocker.run();
        eigenMatchStrategy.process(context);
        asserts.test(context);
    }

    static Stream<Arguments> processCase() {
        Runnable emptyMocker = () -> {};

        Runnable mockerCompareConfig = () -> {
            CompareConfigurationEntity compareConfig = new CompareConfigurationEntity();
            CompareConfigurationEntity.ConfigComparisonExclusionsEntity exclusion = new CompareConfigurationEntity.ConfigComparisonExclusionsEntity();
            exclusion.setCategoryType(MockCategoryType.DYNAMIC_CLASS.getName());
            exclusion.setOperationName("mock");
            exclusion.setExclusionList(new HashSet<>(new ArrayList<>()));
            compareConfig.setComparisonExclusions(Collections.singletonList(exclusion));
            Mockito.when(config.getCompareConfiguration()).thenReturn(compareConfig);
        };

        Supplier<MatchStrategyContext> contextSupplier1 = () -> {
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
            return context;
        };
        Supplier<MatchStrategyContext> contextSupplier2 = () -> {
            MatchStrategyContext context = contextSupplier1.get();
            context.getRecordList().get(0).setMatched(true);
            return context;
        };

        Predicate<MatchStrategyContext> asserts1 = context -> !context.isInterrupt();
        Predicate<MatchStrategyContext> asserts2 = MatchStrategyContext::isInterrupt;

        return Stream.of(
                arguments(emptyMocker, contextSupplier1.get(), asserts1),
                arguments(mockerCompareConfig, contextSupplier2.get(), asserts2)
        );
    }
}