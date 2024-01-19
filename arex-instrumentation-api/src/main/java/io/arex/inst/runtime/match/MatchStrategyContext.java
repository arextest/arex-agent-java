package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.model.MergeDTO;

import java.util.List;

public class MatchStrategyContext {
    private Mocker requestMocker;
    private List<MergeDTO> mergeReplayList;
    private MockStrategyEnum mockStrategy;
    private boolean interrupt;
    private Mocker matchMocker;

    public MatchStrategyContext(Mocker requestMocker, List<MergeDTO> mergeReplayList, MockStrategyEnum mockStrategy) {
        this.requestMocker = requestMocker;
        this.mergeReplayList = mergeReplayList;
        this.mockStrategy = mockStrategy;
    }

    public Mocker getRequestMocker() {
        return requestMocker;
    }

    public void setRequestMocker(Mocker requestMocker) {
        this.requestMocker = requestMocker;
    }

    public List<MergeDTO> getMergeReplayList() {
        return mergeReplayList;
    }

    public void setMergeReplayList(List<MergeDTO> mergeReplayList) {
        this.mergeReplayList = mergeReplayList;
    }

    public MockStrategyEnum getMockStrategy() {
        return mockStrategy;
    }

    public void setMockStrategy(MockStrategyEnum mockStrategy) {
        this.mockStrategy = mockStrategy;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public Mocker getMatchMocker() {
        return matchMocker;
    }

    public void setMatchMocker(Mocker matchMocker) {
        this.matchMocker = matchMocker;
    }
}
