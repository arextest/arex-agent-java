package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.MergeDTO;

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
