package io.arex.inst.runtime.match.strategy;

import io.arex.inst.runtime.match.MatchStrategyContext;
import io.arex.inst.runtime.model.MatchStrategyEnum;

public class EigenMatchStrategy extends AbstractMatchStrategy{

    /**
     * search by eigen value of request
     */
    void process(MatchStrategyContext context) {
        context.setMatchStrategy(MatchStrategyEnum.EIGEN);
        // to be implemented after database merge replay support
        // TODO if match null, whether to return last one(order)
    }
}
