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
            mocker.setCategory(category);
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
