package io.arex.cli.server.handler;

import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.ServletMocker;
import io.arex.foundation.services.StorageService;

import java.util.Map;

public class DebugHandler extends ApiHandler {
    @Override
    public String process(String args) throws Exception {
        ServletMocker mocker = new ServletMocker();
        mocker.setCaseId(args);
        AbstractMocker resultMocker = StorageService.INSTANCE.query(mocker);
        if (resultMocker == null) {
            return "query no result.";
        }
        Map<String, String> responseMap = request((ServletMocker)resultMocker);
        if (responseMap == null) {
            return "response is null.";
        }

        return responseMap.get("responseBody");
    }
}
