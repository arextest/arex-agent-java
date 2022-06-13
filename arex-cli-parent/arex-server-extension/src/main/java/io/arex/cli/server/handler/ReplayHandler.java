package io.arex.cli.server.handler;

import io.arex.foundation.internal.Pair;
import io.arex.foundation.model.*;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.services.StorageService;
import io.arex.foundation.util.CollectionUtil;
import io.arex.foundation.util.DiffUtils;
import io.arex.foundation.util.StringUtil;
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
        ServletMocker mocker = new ServletMocker();
        List<AbstractMocker> mockerList = StorageService.INSTANCE.queryList(mocker, num);
        if (CollectionUtil.isEmpty(mockerList)) {
            LOGGER.warn("query no result.");
            return null;
        }
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (AbstractMocker mockerInfo : mockerList) {
            Map<String, String> responseMap = request((ServletMocker)mockerInfo);
            pairs.add(Pair.of(mockerInfo.getCaseId(), responseMap.get("arex-replay-id")));
        }
        return pairs;
    }

    public List<DiffMocker> computeDiff(List<Pair<String, String>> idPairs) {
        if (CollectionUtil.isEmpty(idPairs)) {
            return null;
        }
        List<DiffMocker> diffSummaryList = new ArrayList<>();
        DiffUtils dmp = new DiffUtils();
        List<AbstractMocker> recordList;
        List<AbstractMocker> replayList;
        String recordJson;
        String replayJson;
        for (Pair<String, String> idPair : idPairs) {
            boolean hasDiff = false;
            int diffCount = 0;
            List<DiffMocker> diffDetailList = new ArrayList<>();
            for (MockerCategory category : MockerCategory.values()) {
                AbstractMocker mocker = generateMocker(category);
                if (mocker == null) {
                    continue;
                }
                mocker.setCaseId(idPair.getFirst());
                recordList = StorageService.INSTANCE.queryList(mocker, 0);
                mocker.setReplayId(idPair.getSecond());
                replayList = StorageService.INSTANCE.queryList(mocker, 0);
                if (CollectionUtil.isEmpty(recordList) && CollectionUtil.isEmpty(replayList)) {
                    continue;
                }
                int len = Math.max(recordList.size(), replayList.size());
                for (int i = 0; i < len; i++) {
                    recordJson = getCompareJson(recordList, i, category);
                    replayJson = getCompareJson(replayList, i, category);
                    if (recordJson == null && replayJson == null) {
                        continue;
                    }
                    Pair<String, String> dbDiffPair = dmp.diff(StringUtil.formatJson(recordJson), StringUtil.formatJson(replayJson));
                    diffCount += dmp.diffCount(dbDiffPair);
                    diffDetailList.add(getDiffMocker(idPair, dbDiffPair, category));
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

    private AbstractMocker generateMocker(MockerCategory category) {
        switch (category) {
            case SERVLET_ENTRANCE:
                return new ServletMocker();
            case DATABASE:
                return new DatabaseMocker();
            case SERVICE_CALL:
                return new HttpClientMocker();
            case REDIS:
                return new JedisMocker();
        }
        return null;
    }

    private String getCompareJson(List<AbstractMocker> mockerList, int index, MockerCategory category) {
        Map<String, String> compareMap = new HashMap<>();
        if (mockerList.size() > index) {
            switch (category) {
                case SERVLET_ENTRANCE:
                    ServletMocker servletMocker = (ServletMocker)mockerList.get(index);
                    compareMap.put("response", servletMocker.getResponse());
                    break;
                case DATABASE:
                    DatabaseMocker databaseMocker = (DatabaseMocker)mockerList.get(index);
                    compareMap.put("dbname", databaseMocker.getDbName());
                    compareMap.put("parameters", databaseMocker.getParameters());
                    compareMap.put("sql", databaseMocker.getSql());
                    break;
                case DYNAMIC_CLASS:
                    DynamicClassMocker dynamicClassMocker = (DynamicClassMocker)mockerList.get(index);
                    compareMap.put("className", dynamicClassMocker.getClazzName());
                    compareMap.put("method", dynamicClassMocker.getOperation());
                    compareMap.put("parameter", dynamicClassMocker.getOperationKey());
                    break;
                case SERVICE_CALL:
                    HttpClientMocker httpClientMocker = (HttpClientMocker)mockerList.get(index);
                    compareMap.put("url", httpClientMocker.getUrl());
                    compareMap.put("request", httpClientMocker.getRequest());
                    break;
                case REDIS:
                    JedisMocker jedisMocker = (JedisMocker)mockerList.get(index);
                    compareMap.put("clusterName", jedisMocker.getClusterName());
                    compareMap.put("key", jedisMocker.getRedisKey());
                    break;
            }
        }
        if (compareMap.size() > 1) {
            return SerializeUtils.serialize(compareMap);
        }
        return compareMap.values().stream().findFirst().orElse(null);
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
