package io.arex.cli.server.handler;

import io.arex.foundation.internal.Pair;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockerCategory;
import io.arex.foundation.model.ServletMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.CollectionUtil;
import io.arex.foundation.util.DiffUtils;
import io.arex.foundation.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplayHandler extends ApiHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplayHandler.class);

    @Override
    public String process(String args) throws Exception {
        List<Pair<String, String>> idPairs = replay(Integer.parseInt(args));
        if (CollectionUtil.isEmpty(idPairs)) {
            LOGGER.warn("replay no result");
            return null;
        }
        List<DiffMocker> diffList = computeDiff(idPairs);
        if (CollectionUtil.isEmpty(diffList)) {
            return null;
        }
        return SerializeUtils.serialize(diffList);
    }

    private List<Pair<String, String>> replay(int num) {
        if (StorageService.INSTANCE == null) {
            LOGGER.warn("storage service unavailable!");
            return null;
        }
        List<String> mockerList = StorageService.INSTANCE.queryReplayBatch(new ServletMocker(), num);
        if (CollectionUtil.isEmpty(mockerList)) {
            LOGGER.warn("query no result.");
            return null;
        }
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (String mockerInfo : mockerList) {
            if (StringUtils.isBlank(mockerInfo) || "{}".equals(mockerInfo)) {
                LOGGER.warn("mockerInfo is not exist.");
                continue;
            }

            ServletMocker servletMocker = SerializeUtils.deserialize(mockerInfo, ServletMocker.class);
            if (servletMocker == null) {
                LOGGER.warn("deserialize mocker is null.");
                continue;
            }
            Map<String, String> responseMap = request(servletMocker);
            pairs.add(Pair.of(servletMocker.getCaseId(), responseMap.get("arex-replay-id")));
        }
        return pairs;
    }

    public List<DiffMocker> computeDiff(List<Pair<String, String>> idPairs) {
        if (CollectionUtil.isEmpty(idPairs)) {
            return null;
        }
        List<DiffMocker> diffSummaryList = new ArrayList<>();
        DiffUtils dmp = new DiffUtils();
        for (Pair<String, String> idPair : idPairs) {
            boolean hasDiff = false;
            int diffCount = 0;
            List<DiffMocker> diffDetailList = new ArrayList<>();
            for (MockerCategory category : MockerCategory.values()) {
                String record;
                String replay;
                List<Map<String, String>> resultList;
                switch (category) {
                    case SERVLET_ENTRANCE:
                        resultList = StorageService.INSTANCE.query(category, idPair.getFirst(), idPair.getSecond());
                        if (CollectionUtil.isEmpty(resultList)) {
                            continue;
                        }
                        record = resultList.get(0).get("RECORDRESPONSE");
                        replay = resultList.get(0).get("REPLAYRESPONSE");
                        if (StringUtil.isEmpty(record) || StringUtil.isEmpty(replay)) {
                            continue;
                        }
                        Pair<String, String> diffPair = dmp.diff(StringUtil.formatJson(record), StringUtil.formatJson(replay));
                        diffCount += dmp.diffCount(diffPair);
                        diffDetailList.add(getDiffMocker(idPair, diffPair, category));
                        break;
                    case DATABASE:
                        resultList = StorageService.INSTANCE.query(category, idPair.getFirst(), idPair.getSecond());
                        if (CollectionUtil.isEmpty(resultList)) {
                            continue;
                        }
                        for (Map<String, String> resultMap : resultList) {
                            Map<String, String> recordMap = new HashMap<>();
                            Map<String, String> replayMap = new HashMap<>();
                            resultMap.forEach((key, val) -> {
                                if (key.startsWith("RECORD")) {
                                    key = StringUtils.substringAfter(key, "RECORD").toLowerCase();
                                    recordMap.put(key, val);
                                } else if (key.startsWith("REPLAY")) {
                                    key = StringUtils.substringAfter(key, "REPLAY").toLowerCase();
                                    replayMap.put(key, val);
                                }
                            });
                            record = SerializeUtils.serialize(recordMap);
                            replay = SerializeUtils.serialize(replayMap);
                            if (StringUtil.isEmpty(record) || StringUtil.isEmpty(replay)) {
                                continue;
                            }
                            Pair<String, String> dbDiffPair = dmp.diff(StringUtil.formatJson(record), StringUtil.formatJson(replay));
                            diffCount += dmp.diffCount(dbDiffPair);
                            diffDetailList.add(getDiffMocker(idPair, dbDiffPair, category));
                        }
                        break;
                }
                if (diffCount > 0) {
                    hasDiff = true;
                }
            }
            if (hasDiff) {
                StorageService.INSTANCE.saveList(diffDetailList);

                DiffMocker diffMocker = new DiffMocker();
                diffMocker.setCaseId(idPair.getFirst());
                diffMocker.setReplayId(idPair.getSecond());
                diffMocker.setDiffCount(diffCount);
                diffSummaryList.add(diffMocker);
            }
        }

        return diffSummaryList;
    }

    public DiffMocker getDiffMocker(Pair<String, String> idPair, Pair<String, String> diffPair, MockerCategory category) {
        DiffMocker diffMocker = new DiffMocker(category);
        diffMocker.setCaseId(idPair.getFirst());
        diffMocker.setReplayId(idPair.getSecond());
        diffMocker.setRecordDiff(diffPair.getFirst());
        diffMocker.setReplayDiff(diffPair.getSecond());
        return diffMocker;
    }
}
