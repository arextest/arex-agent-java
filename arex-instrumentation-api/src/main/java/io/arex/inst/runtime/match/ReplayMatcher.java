package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.util.MockUtils;

import java.util.*;

public class ReplayMatcher {
    private static final String MATCH_TITLE = "replay.match";

    private ReplayMatcher() {
    }

    public static Mocker match(Mocker requestMocker, MockStrategyEnum mockStrategy) {
        Map<Integer, List<MergeDTO>> cachedReplayResultMap = ContextManager.currentContext().getCachedReplayResultMap();
        // first match methodRequestTypeHash: category + operationName + requestType, ensure the same method
        List<MergeDTO> mergeReplayList = cachedReplayResultMap.get(MockUtils.methodRequestTypeHash(requestMocker));
        if (CollectionUtil.isEmpty(mergeReplayList)) {
            return null;
        }

        List<AbstractMatchStrategy> matchStrategyList = MatchStrategyRegister.getMatchStrategies(requestMocker.getCategoryType());
        MatchStrategyContext context = new MatchStrategyContext(requestMocker, mergeReplayList, mockStrategy);
        for (AbstractMatchStrategy matchStrategy : matchStrategyList) {
            matchStrategy.match(context);
        }

        Mocker matchedMocker = context.getMatchMocker();
        String message = StringUtil.format("%s, match strategy: %s",
                requestMocker.logBuilder().toString(), context.getMatchStrategy().name());
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
