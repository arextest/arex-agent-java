package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.model.MatchStrategyEnum;
import io.arex.inst.runtime.util.MockUtils;

import java.util.ArrayList;
import java.util.List;

public class AccurateMatchStrategy extends AbstractMatchStrategy{
    private static final String ACCURATE_MATCH_TITLE = "replay.match.accurate";
    /**
     * search by operationName + requestBody
     * priority:
     * 1. if matching and not matched before return directly
     * 2. if matched before and find-last mode, return matched one
     * 3. if matched multiple result, give next fuzzy match
     * 4. if strict match mode and not matched, interrupt
     */
    void process(MatchStrategyContext context) {
        context.setMatchStrategy(MatchStrategyEnum.ACCURATE);
        Mocker requestMocker = context.getRequestMocker();
        List<Mocker> replayList = context.getReplayList();
        // operationName + requestBody
        int methodSignatureHash = MockUtils.methodSignatureHash(requestMocker);
        List<Mocker> matchedList = new ArrayList<>(replayList.size());
        for (Mocker mocker : replayList) {
            if (methodSignatureHash == mocker.getMethodSignatureHash()) {
                matchedList.add(mocker);
            }
        }
        int matchedCount = matchedList.size();

        if (matchedCount == 1) {
            Mocker matchMocker = matchedList.get(0);
            // unmatched or matched but find-last mode (like dynamicClass)
            if (!matchMocker.isMatched() || MockStrategyEnum.FIND_LAST == context.getMockStrategy()) {
                matchMocker.setMatched(true);
                context.setMatchMocker(matchMocker);
            } else {
                context.setReason("accurate match one result, but it has already been matched before, so cannot be used");
            }
            // other modes can only be matched once, so interrupt and not continue next fuzzy match
            context.setInterrupt(true);
            return;
        }
        // matched multiple result(like as redis: incr、decr) only retain matched item for next fuzzy match
        if (matchedCount > 1) {
            context.setReplayList(matchedList);
            return;
        }
        // if strict match mode and not matched, interrupt and not continue next fuzzy match
        if (MockStrategyEnum.STRICT_MATCH == context.getMockStrategy()) {
            context.setInterrupt(true);
        }
    }

    @Override
    boolean internalCheck(MatchStrategyContext context) {
        // if no request params, do next fuzzy match directly
        return StringUtil.isNotEmpty(context.getRequestMocker().getTargetRequest().getBody());
    }
}
