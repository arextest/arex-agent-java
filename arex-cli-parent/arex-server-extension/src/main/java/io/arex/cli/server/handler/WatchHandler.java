package io.arex.cli.server.handler;

import io.arex.foundation.internal.Pair;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockerCategory;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WatchHandler extends ApiHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchHandler.class);

    @Override
    public String process(String args) throws Exception {
        List<DiffMocker> diffList = new ArrayList<>();
        List<Pair<String, String>> resultList;
        DiffMocker mocker = new DiffMocker();
        for (MockerCategory category : MockerCategory.values()) {
            mocker.setReplayId(args);
            switch (category) {
                case SERVLET_ENTRANCE:
                case DATABASE:
                    mocker.setCategory(category);
                    resultList = StorageService.INSTANCE.queryList(mocker);
                    if (CollectionUtil.isEmpty(resultList)) {
                        continue;
                    }
                    for (Pair<String, String> resultPair : resultList) {
                        DiffMocker diffMocker = new DiffMocker();
                        diffMocker.setReplayId(args);
                        diffMocker.setCategory(category);
                        diffMocker.setRecordDiff(resultPair.getFirst());
                        diffMocker.setReplayDiff(resultPair.getSecond());
                        diffList.add(diffMocker);
                    }
                    break;
            }
        }
        if (CollectionUtil.isEmpty(diffList)) {
            return null;
        }
        return SerializeUtils.serialize(diffList);
    }
}
