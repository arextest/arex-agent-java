package io.arex.cli.server.handler;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.foundation.model.DiffMocker;
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
        for (MockCategoryType category : MockCategoryType.values()) {
            mocker.setReplayId(args);
            mocker.setCategoryType(category);
            List<DiffMocker> diffMockers = StorageService.INSTANCE.queryList(mocker);
            if (CollectionUtil.isNotEmpty(diffMockers)) {
                diffList.addAll(diffMockers);
            }
        }
        if (CollectionUtil.isEmpty(diffList)) {
            return null;
        }
        return SerializeUtils.serialize(diffList);
    }
}