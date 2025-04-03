package io.arex.inst.runtime.match.key;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;

public class DefaultMatchKeyBuilderImpl implements MatchKeyBuilder {

    @Override
    public boolean isSupported(MockCategoryType categoryType) {
        return true;
    }

    /**
     * category + operationName + requestType
     */
    @Override
    public int getFuzzyMatchKey(Mocker mocker) {
        return StringUtil.encodeAndHash(
                mocker.getCategoryType().getName(),
                mocker.getOperationName(),
                mocker.getTargetRequest().getType());
    }

    /**
     * operationName + requestBody
     * (operationName can remove, but need compatible with old logic, so keep it)
     */
    @Override
    public int getAccurateMatchKey(Mocker mocker) {
        return StringUtil.encodeAndHash(
                mocker.getOperationName(),
                mocker.getTargetRequest().getBody());
    }

    /**
     * requestBody
     */
    @Override
    public String getEigenBody(Mocker mocker) {
        return mocker.getTargetRequest().getBody();
    }
}
