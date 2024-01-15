package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.util.CollectionUtil;

import java.util.*;

public class MatchStrategyRegister {
    private static final AbstractMatchStrategy ACCURATE_STRATEGY = new AccurateMatchStrategy();
    private static final AbstractMatchStrategy FUZZY_STRATEGY = new FuzzyMatchStrategy();
    private static final AbstractMatchStrategy EIGEN_STRATEGY = new EigenMatchStrategy();
    private static final Map<String, List<AbstractMatchStrategy>> MATCH_STRATEGIES = register();

    private MatchStrategyRegister() {
    }

    public static List<AbstractMatchStrategy> getMatchStrategies(MockCategoryType categoryType) {
        return MATCH_STRATEGIES.get(categoryType.getName());
    }

    private static Map<String, List<AbstractMatchStrategy>> register() {
        Map<String, List<AbstractMatchStrategy>> strategyMap = new HashMap<>();
        strategyMap.put(MockCategoryType.DYNAMIC_CLASS.getName(), CollectionUtil.newArrayList(ACCURATE_STRATEGY, FUZZY_STRATEGY));
        strategyMap.put(MockCategoryType.REDIS.getName(), CollectionUtil.newArrayList(ACCURATE_STRATEGY, FUZZY_STRATEGY));
        strategyMap.put(MockCategoryType.DATABASE.getName(), CollectionUtil.newArrayList(ACCURATE_STRATEGY, EIGEN_STRATEGY));
        return strategyMap;
    }
}
