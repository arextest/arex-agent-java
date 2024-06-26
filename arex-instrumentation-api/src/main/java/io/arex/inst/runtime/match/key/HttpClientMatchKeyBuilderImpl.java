package io.arex.inst.runtime.match.key;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.serializer.Serializer;

import java.util.HashMap;
import java.util.Map;

public class HttpClientMatchKeyBuilderImpl implements MatchKeyBuilder {

    @Override
    public boolean isSupported(MockCategoryType categoryType) {
        return MockCategoryType.HTTP_CLIENT.getName().equals(categoryType.getName());
    }

    /**
     * operationName + httpMethod
     */
    @Override
    public int getFuzzyMatchKey(Mocker mocker) {
        String operationName = mocker.getOperationName();
        String httpMethod = mocker.getTargetRequest().attributeAsString(ArexConstants.HTTP_METHOD);
        return StringUtil.encodeAndHash(
                operationName,
                httpMethod);
    }


    /**
     * queryString + requestBody
     */
    @Override
    public int getAccurateMatchKey(Mocker mocker) {
        String queryString = mocker.getTargetRequest().attributeAsString(ArexConstants.HTTP_QUERY_STRING);
        String requestBody = mocker.getTargetRequest().getBody();
        return StringUtil.encodeAndHash(
                queryString,
                requestBody);
    }

    /**
     * queryString + requestBody
     */
    @Override
    public String getEigenBody(Mocker mocker) {
        String queryString = mocker.getTargetRequest().attributeAsString(ArexConstants.HTTP_QUERY_STRING);
        Map<String, String> objectNode = new HashMap<>();
        if (StringUtil.isNotEmpty(queryString)) {
            objectNode.put(ArexConstants.HTTP_QUERY_STRING, queryString);
        }
        objectNode.put(ArexConstants.HTTP_BODY, mocker.getTargetRequest().getBody());
        return Serializer.serialize(objectNode);
    }
}
