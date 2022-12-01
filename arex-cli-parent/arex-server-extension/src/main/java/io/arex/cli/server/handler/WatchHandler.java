package io.arex.cli.server.handler;

import com.arextest.model.mock.MockCategoryType;
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
        for (MockCategoryType category : MockCategoryType.DEFAULTS) {
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