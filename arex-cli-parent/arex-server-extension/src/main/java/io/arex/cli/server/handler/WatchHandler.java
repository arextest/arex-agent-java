package io.arex.cli.server.handler;

import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockerCategory;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public class WatchHandler extends ApiHandler {

    @Override
    public String process(String args) throws Exception {
        List<DiffMocker> diffList = new ArrayList<>();
        DiffMocker mocker = new DiffMocker();
        for (MockerCategory category : MockerCategory.values()) {
            mocker.setReplayId(args);
            switch (category) {
                case SERVLET_ENTRANCE:
                case DATABASE:
                case DYNAMIC_CLASS:
                    mocker.setCategory(category);
                    diffList.addAll(StorageService.INSTANCE.queryList(mocker));
                    break;
            }
        }
        if (CollectionUtil.isEmpty(diffList)) {
            return null;
        }
        return SerializeUtils.serialize(diffList);
    }
}
