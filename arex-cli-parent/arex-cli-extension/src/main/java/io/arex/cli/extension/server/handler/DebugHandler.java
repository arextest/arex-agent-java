package io.arex.cli.extension.server.handler;

import io.arex.cli.api.extension.CliDataStorage;
import io.arex.foundation.extension.ExtensionLoader;
import io.arex.foundation.model.ServletMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.StringUtil;

import java.util.Map;

public class DebugHandler extends ApiHandler {
    private final CliDataStorage storageService = ExtensionLoader.getExtension(CliDataStorage.class);

    @Override
    public String process(String args) throws Exception {
        ServletMocker mocker = new ServletMocker();
        mocker.setCaseId(args);
        String mockerData = storageService.query(mocker);
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
