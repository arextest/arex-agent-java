package io.arex.inst.runtime.match.strategy;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.match.MatchStrategyContext;
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
        List<Mocker> recordList = context.getRecordList();
        Mocker resultMocker = null;
        int size = recordList.size();
        for (int i = 0; i < size; i++) {
            Mocker recordMocker = recordList.get(i);
            if (!recordMocker.isMatched()) {
                resultMocker = recordMocker;
                break;
            }
        }
        if (resultMocker == null && MockStrategyEnum.FIND_LAST == context.getMockStrategy()) {
            resultMocker = recordList.get(size - 1);
        }
        setContextResult(context, resultMocker, "fuzzy match no result, all has been matched");
    }

    @Override
    boolean internalCheck(MatchStrategyContext context) {
        return CollectionUtil.isNotEmpty(context.getRecordList());
    }
}
