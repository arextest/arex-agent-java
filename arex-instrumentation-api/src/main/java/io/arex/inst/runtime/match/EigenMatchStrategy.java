package io.arex.inst.runtime.match;

public class EigenMatchStrategy extends AbstractMatchStrategy{

    /**
     * search by eigen value of request
     */
    void process(MatchStrategyContext context) {
        // to be implemented after database merge replay support
    }

    int order() {
        return EIGEN_MATCH_ORDER;
    }
}
