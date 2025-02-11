package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;

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
        // first match methodRequestTypeHash: category + operationName + requestType, ensure the same method
        List<Mocker> replayList = cachedReplayResultMap.get(MockUtils.methodRequestTypeHash(requestMocker));
        if (CollectionUtil.isEmpty(replayList)) {
            LogManager.warn(MATCH_TITLE, StringUtil.format("match no result, not exist this method signature, " +
                            "check if it has been recorded, categoryType: %s, operationName: %s, requestBody: %s",
                    requestMocker.getCategoryType().getName(), requestMocker.getOperationName(), requestMocker.getTargetRequest().getBody()));
            return null;
        }

        List<AbstractMatchStrategy> matchStrategyList = MatchStrategyRegister.getMatchStrategies(requestMocker.getCategoryType());
        MatchStrategyContext context = new MatchStrategyContext(requestMocker, replayList, mockStrategy);
        for (AbstractMatchStrategy matchStrategy : matchStrategyList) {
            matchStrategy.match(context);
        }

        logMatchResult(context);

        return context.getMatchMocker();
    }

    private static void logMatchResult(MatchStrategyContext context) {
        Mocker matchedMocker = context.getMatchMocker();
        Mocker requestMocker = context.getRequestMocker();

        StringBuilder matchResult = new StringBuilder(requestMocker.getCategoryType().getName());
        matchResult.append(":").append(requestMocker.getOperationName());
        if (matchedMocker != null) {
            matchResult.append(" match success");
        } else {
            matchResult.append(" match fail, reason: ").append(context.getReason());
        }

        String message = StringUtil.format("%s %n%s, requestType: %s, match strategy: %s, mock strategy: %s",
                matchResult.toString(),
                requestMocker.logBuilder().toString(),
                requestMocker.getTargetRequest().getType(),
                (context.getMatchStrategy() != null ? context.getMatchStrategy().name() : StringUtil.EMPTY),
                context.getMockStrategy().name());

        if (Config.get().isEnableDebug()) {
            message += StringUtil.format("%nrequest: %s%nresponse: %s",
                    Serializer.serialize(requestMocker), Serializer.serialize(matchedMocker));
        }
        LogManager.info(MATCH_TITLE, message);
    }
}
