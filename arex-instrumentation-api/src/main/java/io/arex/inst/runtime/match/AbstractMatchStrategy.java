package io.arex.inst.runtime.match;

import io.arex.inst.runtime.log.LogManager;

public abstract class AbstractMatchStrategy {
    static final String MATCH_TITLE = "replay.match.fail";

    public void match(MatchStrategyContext context) {
        try {
            if (support(context)) {
                process(context);
            }
        } catch (Exception e) {
            LogManager.warn(MATCH_TITLE, e);
        }
    }

    private boolean support(MatchStrategyContext context) {
        if (context == null || context.getRequestMocker() == null || context.isInterrupt()) {
            return false;
        }
        return internalCheck(context);
    }

    boolean internalCheck(MatchStrategyContext context) {
        return true;
    }
    abstract void process(MatchStrategyContext context) throws Exception;
}
