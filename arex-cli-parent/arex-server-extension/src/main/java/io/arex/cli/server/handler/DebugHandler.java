package io.arex.cli.server.handler;

import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.ServiceEntranceMocker;
import io.arex.foundation.services.StorageService;

import java.util.Map;

public class DebugHandler extends ApiHandler {
    @Override
    public String process(String args) throws Exception {
        ServiceEntranceMocker mocker = new ServiceEntranceMocker();
        mocker.setCaseId(args);
        AbstractMocker resultMocker = StorageService.INSTANCE.query(mocker);
        if (resultMocker == null) {
            return "query no result.";
        }
        Map<String, String> responseMap = request((ServiceEntranceMocker)resultMocker);
        if (responseMap == null) {
            return "response is null.";
        }

        return responseMap.get("responseBody");
    }
}
