package io.arex.cli.extension.server.handler;

import io.arex.cli.api.extension.CliDataStorage;
import io.arex.foundation.extension.ExtensionLoader;
import io.arex.foundation.internal.Pair;
import io.arex.cli.api.model.DiffMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WatchHandler extends ApiHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchHandler.class);
    private final CliDataStorage storageService = ExtensionLoader.getExtension(CliDataStorage.class);

    @Override
    public String process(String args) throws Exception {
        List<DiffMocker> diffList = new ArrayList<>();
        List<Pair<String, String>> resultList;
        DiffMocker mocker = new DiffMocker();

        for (String category : storageService.getSupportCategory()) {
            mocker.setReplayId(args);
            switch (category) {
                case "ServletEntrance":
                case "Database":
                    mocker.setCategory(category);
                    resultList = storageService.queryList(mocker);
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
                default:
                    break;
            }
        }
        if (CollectionUtil.isEmpty(diffList)) {
            return null;
        }
        return SerializeUtils.serialize(diffList);
    }
}
