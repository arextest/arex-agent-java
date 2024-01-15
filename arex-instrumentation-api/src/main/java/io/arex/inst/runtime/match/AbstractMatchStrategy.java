package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.MergeDTO;

public abstract class AbstractMatchStrategy {
    static final String MATCH_TITLE = "replay.match.fail";
    static final int ACCURATE_MATCH_ORDER = 10;
    static final int FUZZY_MATCH_ORDER = 20;
    static final int EIGEN_MATCH_ORDER = 30;

    public void match(MatchStrategyContext context) {
        try {
            if (check(context)) {
                process(context);
            }
        } catch (Exception e) {
            LogManager.warn(MATCH_TITLE, e);
        }
    }

    private boolean check(MatchStrategyContext context) {
        if (context == null || context.getRequestMocker() == null || context.isInterrupt()) {
            return false;
        }
        return valid(context);
    }

    boolean valid(MatchStrategyContext context) {
        return true;
    }
    abstract int order();
    abstract void process(MatchStrategyContext context) throws Exception;

    Mocker buildMatchedMocker(Mocker requestMocker, MergeDTO mergeReplayDTO) {
        if (mergeReplayDTO == null) {
            return null;
        }
        requestMocker.getTargetResponse().setBody(mergeReplayDTO.getResponse());
        requestMocker.getTargetResponse().setType(mergeReplayDTO.getResponseType());
        requestMocker.getTargetResponse().setAttributes(mergeReplayDTO.getResponseAttributes());
        mergeReplayDTO.setMatched(true);
        return requestMocker;
    }
}
