package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.match.strategy.AbstractMatchStrategy;
import io.arex.inst.runtime.match.strategy.AccurateMatchStrategy;
import io.arex.inst.runtime.match.strategy.EigenMatchStrategy;
import io.arex.inst.runtime.match.strategy.FuzzyMatchStrategy;

import java.util.*;

public class MatchStrategyRegister {
    private static final AbstractMatchStrategy ACCURATE_STRATEGY = new AccurateMatchStrategy();
    private static final AbstractMatchStrategy FUZZY_STRATEGY = new FuzzyMatchStrategy();
    private static final AbstractMatchStrategy EIGEN_STRATEGY = new EigenMatchStrategy();

    private static final List<AbstractMatchStrategy> ACCURATE_FUZZY_COMBINE = CollectionUtil.newArrayList(ACCURATE_STRATEGY, FUZZY_STRATEGY);

    private static final List<AbstractMatchStrategy> DEFAULT_MATCH_COMBINE = CollectionUtil.newArrayList(ACCURATE_STRATEGY, EIGEN_STRATEGY);

    private static final Map<String, List<AbstractMatchStrategy>> MATCH_STRATEGIES = register();

    private MatchStrategyRegister() {
    }

    public static List<AbstractMatchStrategy> getMatchStrategies(Mocker mocker) {
        List<AbstractMatchStrategy> matchStrategies = MATCH_STRATEGIES.get(mocker.getCategoryType().getName());
        if (matchStrategies == null) {
            return DEFAULT_MATCH_COMBINE;
        }
        return matchStrategies;
    }

    private static Map<String, List<AbstractMatchStrategy>> register() {
        Map<String, List<AbstractMatchStrategy>> strategyMap = new HashMap<>();
        strategyMap.put(MockCategoryType.DYNAMIC_CLASS.getName(), ACCURATE_FUZZY_COMBINE);
        strategyMap.put(MockCategoryType.REDIS.getName(), ACCURATE_FUZZY_COMBINE);
        // other category type such as:database、httpclient use default match strategy: accurate and eigen match
        return strategyMap;
    }
}
