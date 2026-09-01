package io.arex.inst.runtime.match.key;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;

public interface MatchKeyBuilder {
    boolean isSupported(MockCategoryType categoryType);

    int getFuzzyMatchKey(Mocker mocker);

    int getAccurateMatchKey(Mocker mocker);

    String getEigenBody(Mocker mocker);
}
