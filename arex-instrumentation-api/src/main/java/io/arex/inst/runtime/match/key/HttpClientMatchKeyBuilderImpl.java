package io.arex.inst.runtime.match.key;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.model.ArexConstants;

public class HttpClientMatchKeyBuilderImpl implements MatchKeyBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
        if (StringUtil.isNotEmpty(queryString)) {
            objectNode.put(ArexConstants.HTTP_QUERY_STRING, queryString);
        }
        objectNode.put(ArexConstants.HTTP_BODY, mocker.getTargetRequest().getBody());
        return objectNode.toString();
    }
}
