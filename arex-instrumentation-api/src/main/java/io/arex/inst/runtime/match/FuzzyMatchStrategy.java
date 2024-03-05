package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.model.MatchStrategyEnum;
import io.arex.inst.runtime.model.MergeDTO;

import java.util.List;

public class FuzzyMatchStrategy extends AbstractMatchStrategy {
    /**
     * search under the same method signature
     * @return unmatched or last one
     */
    void process(MatchStrategyContext context) {
        context.setMatchStrategy(MatchStrategyEnum.FUZZY);
        Mocker requestMocker = context.getRequestMocker();
        List<MergeDTO> mergeReplayList = context.getMergeReplayList();
        MergeDTO matchedDTO = null;
        int size = mergeReplayList.size();
        for (int i = 0; i < size; i++) {
            MergeDTO mergeReplayDTO = mergeReplayList.get(i);
            if (!mergeReplayDTO.isMatched()) {
                matchedDTO = mergeReplayDTO;
                break;
            }
        }
        if (matchedDTO == null && MockStrategyEnum.FIND_LAST == context.getMockStrategy()) {
            matchedDTO = mergeReplayList.get(size - 1);
        }
        context.setMatchMocker(buildMatchedMocker(requestMocker, matchedDTO));
    }

    @Override
    boolean internalCheck(MatchStrategyContext context) {
        return CollectionUtil.isNotEmpty(context.getMergeReplayList());
    }
}
