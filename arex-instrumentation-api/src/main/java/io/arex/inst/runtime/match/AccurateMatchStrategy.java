package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.util.MockUtils;

import java.util.ArrayList;
import java.util.List;

public class AccurateMatchStrategy extends AbstractMatchStrategy{
    private static final String ACCURATE_MATCH_TITLE = "replay.match.accurate";
    /**
     * search by operationName + requestBody
     */
    void process(MatchStrategyContext context) {
        Mocker requestMocker = context.getRequestMocker();
        List<MergeDTO> mergeReplayList = context.getMergeReplayList();
        int methodSignatureHash = MockUtils.methodSignatureHash(requestMocker);
        List<MergeDTO> matchedList = new ArrayList<>();
        for (MergeDTO mergeDTO : mergeReplayList) {
            if (methodSignatureHash == mergeDTO.getMethodSignatureHash()) {
                matchedList.add(mergeDTO);
            }
        }
        int matchedCount = matchedList.size();
        /*
         * 1. unmatched
         * 2. matched but find last mode (like dynamicClass)
         */
        if (matchedCount == 1) {
            if (!matchedList.get(0).isMatched() || MockStrategyEnum.FIND_LAST == context.getMockStrategy()) {
                context.setMatchMocker(buildMatchedMocker(requestMocker, matchedList.get(0)));
            } else {
                LogManager.info(ACCURATE_MATCH_TITLE, StringUtil.format("accurate match one result, but cannot be used, " +
                                "reason: matched: %s, mock strategy: %s, methodSignatureHash: %s, category: %s",
                                Boolean.toString(matchedList.get(0).isMatched()), context.getMockStrategy().name(),
                                String.valueOf(methodSignatureHash), requestMocker.getCategoryType().getName()));
            }
            // other modes can only be matched once, so interrupt and not continue next fuzzy match
            context.setInterrupt(true);
        }
        // matched multiple result(like as redis: incr、decr) only retain matched item for next fuzzy match
        if (matchedCount > 1) {
            context.setMergeReplayList(matchedList);
        }
        // if strict match mode and not matched, interrupt and not continue next fuzzy match
        if (matchedCount == 0 && MockStrategyEnum.STRICT_MATCH == context.getMockStrategy()) {
            context.setInterrupt(true);
        }
    }

    @Override
    boolean valid(MatchStrategyContext context) {
        // if no request params, do next fuzzy match directly
        return StringUtil.isNotEmpty(context.getRequestMocker().getTargetRequest().getBody());
    }

    int order() {
        return ACCURATE_MATCH_ORDER;
    }
}
