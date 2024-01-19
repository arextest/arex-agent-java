package io.arex.inst.runtime.model;

import io.arex.agent.bootstrap.model.MockCategoryType;

public enum MergeReplayType {

    DYNAMIC_CLASS(MockCategoryType.DYNAMIC_CLASS),

    REDIS(MockCategoryType.REDIS);

    private MockCategoryType mockCategoryType;
    MergeReplayType(MockCategoryType mockCategoryType) {
        this.mockCategoryType = mockCategoryType;
    }

    public MockCategoryType getMockCategoryType() {
        return mockCategoryType;
    }
}
