package io.arex.cli.server.handler;

import io.arex.foundation.model.MockDataType;
import io.arex.foundation.model.ServletMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.StringUtil;

import java.util.Map;

public class DebugHandler extends ApiHandler {
    @Override
    public String process(String args) throws Exception {
        ServletMocker mocker = new ServletMocker();
        mocker.setCaseId(args);
        String mockerData = StorageService.INSTANCE.query(mocker, MockDataType.RECORD);
        if (StringUtil.isEmpty(mockerData)) {
            return "query no result.";
        }
        mocker = SerializeUtils.deserialize(mockerData, ServletMocker.class);
        if (mocker == null) {
            return "deserialize mocker is null.";
        }
        Map<String, String> responseMap = request(mocker);
        if (responseMap == null) {
            return "response is null.";
        }

        return responseMap.get("responseBody");
    }
}
