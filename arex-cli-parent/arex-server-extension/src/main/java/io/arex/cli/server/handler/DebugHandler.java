package io.arex.cli.server.handler;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.foundation.services.StorageService;

import java.util.Map;

public class DebugHandler extends ApiHandler {
    @Override
    public String process(String args) throws Exception {
        Mocker mocker = new ArexMocker();
        mocker.setReplayId(args);
        Mocker resultMocker = StorageService.INSTANCE.query(mocker);
        if (resultMocker == null) {
            return "query no result.";
        }
        Map<String, String> responseMap = request(resultMocker);
        if (responseMap == null) {
            return "response is null.";
        }

        return responseMap.get("responseBody");
    }
}