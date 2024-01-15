package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.MergeDTO;

import java.util.List;

public class FuzzyMatchStrategy extends AbstractMatchStrategy {
    private static final String FUZZY_MATCH_TITLE = "replay.match.fuzzy";

    /**
     * search under the same method signature
     * @return unmatched or last one
     */
    void process(MatchStrategyContext context) {
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
        if (Config.get().isEnableDebug()) {
            String response = matchedDTO != null ? matchedDTO.getResponse() : StringUtil.EMPTY;
            LogManager.info(FUZZY_MATCH_TITLE, StringUtil.format("%s%nrequest: %s%nresponse: %s",
                    requestMocker.logBuilder().toString(), requestMocker.getTargetRequest().getBody(), response));
        }
        context.setMatchMocker(buildMatchedMocker(requestMocker, matchedDTO));
    }

    @Override
    boolean valid(MatchStrategyContext context) {
        return CollectionUtil.isNotEmpty(context.getMergeReplayList());
    }

    int order() {
        return FUZZY_MATCH_ORDER;
    }
}
