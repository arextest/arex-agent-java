package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.model.MatchStrategyEnum;

import java.util.List;

public class FuzzyMatchStrategy extends AbstractMatchStrategy {
    /**
     * search under the same method signature
     * replayList is arranged in ascending order by creationTime
     * @return not matched before or last one
     */
    void process(MatchStrategyContext context) {
        context.setMatchStrategy(MatchStrategyEnum.FUZZY);
        List<Mocker> replayList = context.getReplayList();
        Mocker mocker = null;
        int size = replayList.size();
        for (int i = 0; i < size; i++) {
            Mocker mockerDTO = replayList.get(i);
            if (!mockerDTO.isMatched()) {
                mocker = mockerDTO;
                break;
            }
        }
        if (mocker == null && MockStrategyEnum.FIND_LAST == context.getMockStrategy()) {
            mocker = replayList.get(size - 1);
        }
        if (mocker != null) {
            mocker.setMatched(true);
        } else {
            context.setReason("fuzzy match no result, all has been matched");
        }

        context.setMatchMocker(mocker);
    }

    @Override
    boolean internalCheck(MatchStrategyContext context) {
        return CollectionUtil.isNotEmpty(context.getReplayList());
    }
}
