package io.arex.inst.runtime.match.strategy;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.match.MatchKeyFactory;
import io.arex.inst.runtime.match.MatchStrategyContext;
import io.arex.inst.runtime.model.MatchStrategyEnum;

import java.util.ArrayList;
import java.util.List;

public class AccurateMatchStrategy extends AbstractMatchStrategy{
    /**
     * search by operationName + requestBody
     * priority:
     * 1. if matching and not matched before return directly
     * 2. if matched before and find-last mode, return matched one
     * 3. if matched multiple result, give next match
     * 4. if strict match mode and not matched, interrupt
     */
    public void process(MatchStrategyContext context) {
        context.setMatchStrategy(MatchStrategyEnum.ACCURATE);
        Mocker requestMocker = context.getRequestMocker();
        List<Mocker> recordList = context.getRecordList();
        // operationName + requestBody
        requestMocker.setAccurateMatchKey(MatchKeyFactory.INSTANCE.getAccurateMatchKey(requestMocker));
        List<Mocker> matchedList = new ArrayList<>(recordList.size());
        for (Mocker recordMocker : recordList) {
            if (requestMocker.getAccurateMatchKey() == recordMocker.getAccurateMatchKey()) {
                matchedList.add(recordMocker);
            }
        }
        int matchedCount = matchedList.size();
        if (matchedCount == 1) {
            Mocker matchMocker = matchedList.get(0);
            // unmatched or matched but find-last mode (eg: dynamicClass)
            if (!matchMocker.isMatched() || MockStrategyEnum.FIND_LAST == context.getMockStrategy()) {
                matchMocker.setMatched(true);
                context.setMatchMocker(matchMocker);
                context.setInterrupt(true);
                return;
            }
        }
        // matched multiple result(like as redis: incr、decr) only retain matched item for next match
        if (matchedCount > 1) {
            context.setRecordList(matchedList);
            return;
        }
        // if strict match mode and not matched, interrupt and not continue next match
        if (MockStrategyEnum.STRICT_MATCH == context.getMockStrategy()) {
            context.setInterrupt(true);
        }
    }

    @Override
    public boolean internalCheck(MatchStrategyContext context) {
        // if no request params, do next match directly
        return StringUtil.isNotEmpty(context.getRequestMocker().getTargetRequest().getBody());
    }
}
