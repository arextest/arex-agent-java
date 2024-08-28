package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.match.strategy.AbstractMatchStrategy;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MatchStrategyEnum;
import io.arex.inst.runtime.model.ReplayCompareResultDTO;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.ReplayUtil;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ReplayMatcher {
    private ReplayMatcher() {
    }

    /**
     * match rule:
     * 1. methodRequestTypeHash: categoryType + operationName + requestType
     * 2. accurate match: operationName + requestBody
     * 3. fuzzy match/eigen match
     */
    public static Mocker match(Mocker requestMocker, MockStrategyEnum mockStrategy) {
        if (MapUtils.isEmpty(ContextManager.currentContext().getCachedReplayResultMap())) {
            return null;
        }

        MatchStrategyContext context = new MatchStrategyContext(requestMocker, mockStrategy);

        doMatch(context);

        logMatchResult(context);

        setCompareResult(context);

        return context.getMatchMocker();
    }

    private static void doMatch(MatchStrategyContext context) {
        Mocker requestMocker = context.getRequestMocker();
        Map<Integer, List<Mocker>> cachedReplayResultMap = ContextManager.currentContext().getCachedReplayResultMap();
        // first fuzzy match, such as: category + operationName + requestType, ensure the same method
        requestMocker.setFuzzyMatchKey(MatchKeyFactory.INSTANCE.getFuzzyMatchKey(requestMocker));
        List<Mocker> recordList = cachedReplayResultMap.get(requestMocker.getFuzzyMatchKey());
        if (CollectionUtil.isEmpty(recordList)) {
            context.setReason("match no result, not exist this method signature, check if it has been recorded");
            return;
        }
        context.setRecordList(recordList);
        int fuzzyMatchResultCount = recordList.size();
        List<AbstractMatchStrategy> matchStrategyList = MatchStrategyRegister.getMatchStrategies(requestMocker, fuzzyMatchResultCount);
        for (AbstractMatchStrategy matchStrategy : matchStrategyList) {
            matchStrategy.match(context);
        }
    }

    private static void logMatchResult(MatchStrategyContext context) {
        Mocker matchedMocker = context.getMatchMocker();
        Mocker requestMocker = context.getRequestMocker();

        if (requestMocker.getCategoryType().isEntryPoint()) {
            return;
        }

        String matchResult;
        if (matchedMocker != null) {
            matchResult = "match success";
        } else {
            matchResult = "match fail" + StringUtil.format(", reason: %s", context.getReason());
        }

        String message = StringUtil.format("%s %n%s, requestType: %s, match strategy: %s, mock strategy: %s",
                matchResult,
                requestMocker.logBuilder().toString(),
                requestMocker.getTargetRequest().getType(),
                (context.getMatchStrategy() != null ? context.getMatchStrategy().name() : StringUtil.EMPTY),
                context.getMockStrategy().name());

        if (Config.get().isEnableDebug()) {
            message += StringUtil.format("%nrequest: %s%nresponse: %s",
                    Serializer.serialize(requestMocker), Serializer.serialize(matchedMocker));
        }
        LogManager.info(ArexConstants.MATCH_LOG_TITLE, message);
    }

    /**
     * compare type:
     * value diff
     * new call (recordMocker is null)
     * (call missing after entry point)
     */
    private static void setCompareResult(MatchStrategyContext context) {
        Mocker replayMocker = context.getRequestMocker();
        boolean isEntryPoint = replayMocker.getCategoryType().isEntryPoint();
        if (replayMocker.getCategoryType().isSkipComparison() && !isEntryPoint) {
            return;
        }

        String recordMsg = null;
        String replayMsg = ReplayUtil.getCompareMessage(replayMocker);
        long recordTime = Long.MAX_VALUE;
        long replayTime = replayMocker.getCreationTime();
        boolean sameMsg = false;
        Mocker recordMocker = context.getMatchMocker();
        String extendMessage = null;
        if (recordMocker != null) {
            recordMsg = ReplayUtil.getCompareMessage(recordMocker);
            recordTime = recordMocker.getCreationTime();
            if (MatchStrategyEnum.ACCURATE.equals(context.getMatchStrategy()) && !isEntryPoint) {
                replayMsg = StringUtil.EMPTY;
                sameMsg = true;
            }
            // for expect-script assert use
            if ("SOAConsumer".equalsIgnoreCase(recordMocker.getCategoryType().getName())) {
                extendMessage = recordMocker.getTargetResponse().getBody();
            }
        }

        LinkedBlockingQueue<ReplayCompareResultDTO> replayCompareResultQueue =
                ContextManager.currentContext().getReplayCompareResultQueue();
        ReplayCompareResultDTO compareResultDTO = ReplayUtil.convertCompareResult(
                replayMocker, recordMsg, replayMsg, recordTime, replayTime, sameMsg);
        compareResultDTO.setExtendMessage(extendMessage);
        replayCompareResultQueue.offer(compareResultDTO);
    }
}
