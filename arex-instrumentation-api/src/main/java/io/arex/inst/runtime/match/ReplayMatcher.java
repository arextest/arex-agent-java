package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.match.strategy.AbstractMatchStrategy;

import java.util.*;

public class ReplayMatcher {
    private static final String MATCH_TITLE = "replay.match";

    private ReplayMatcher() {
    }

    /**
     * match rule:
     * 1. methodRequestTypeHash: categoryType + operationName + requestType
     * 2. accurate match: operationName + requestBody
     * 3. fuzzy match/eigen match
     */
    public static Mocker match(Mocker requestMocker, MockStrategyEnum mockStrategy) {
        Map<Integer, List<Mocker>> cachedReplayResultMap = ContextManager.currentContext().getCachedReplayResultMap();

        // first fuzzy match, such as: category + operationName + requestType, ensure the same method
        List<Mocker> replayList = cachedReplayResultMap.get(MatchKeyFactory.INSTANCE.generateFuzzyMatchKey(requestMocker));
        if (CollectionUtil.isEmpty(replayList)) {
            if (!requestMocker.getCategoryType().isEntryPoint()) {
                LogManager.warn(MATCH_TITLE, StringUtil.format("match no result, categoryType: %s, operationName: %s, requestBody: %s",
                        requestMocker.getCategoryType().getName(), requestMocker.getOperationName(), requestMocker.getTargetRequest().getBody()));
            }
            return null;
        }

        List<AbstractMatchStrategy> matchStrategyList = MatchStrategyRegister.getMatchStrategies(requestMocker);
        MatchStrategyContext context = new MatchStrategyContext(requestMocker, replayList, mockStrategy);
        for (AbstractMatchStrategy matchStrategy : matchStrategyList) {
            matchStrategy.match(context);
        }

        Mocker matchedMocker = context.getMatchMocker();
        String message = StringUtil.format("%s, match strategy: %s, mock strategy: %s",
                requestMocker.logBuilder().toString(),
                (context.getMatchStrategy() != null ? context.getMatchStrategy().name() : StringUtil.EMPTY),
                mockStrategy.name());
        if (StringUtil.isNotEmpty(context.getReason())) {
            message += StringUtil.format(", reason: %s", context.getReason());
        }
        if (Config.get().isEnableDebug()) {
            String response = matchedMocker != null && matchedMocker.getTargetResponse() != null
                    ? matchedMocker.getTargetResponse().getBody() : StringUtil.EMPTY;
            message += StringUtil.format("%nrequest: %s%nresponse: %s",
                    requestMocker.getTargetRequest().getBody(), response);
        }
        LogManager.info(MATCH_TITLE, message);
        return matchedMocker;
    }

}
