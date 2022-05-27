package io.arex.cli.server.handler;

import io.arex.foundation.internal.Pair;
import io.arex.foundation.model.*;
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

    private static final String SQL_TEMPLATE = "SELECT %s FROM %s WHERE %s ORDER BY createTime DESC";

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
        mocker.setMockDataType(MockDataType.RECORD);
        List<String> mockerList = StorageService.INSTANCE.queryList(mocker, num);
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
        List<Map<String, String>> recordList;
        List<Map<String, String>> replayList;
        String recordJson;
        String replayJson;
        for (Pair<String, String> idPair : idPairs) {
            boolean hasDiff = false;
            int diffCount = 0;
            List<DiffMocker> diffDetailList = new ArrayList<>();
            for (MockerCategory category : MockerCategory.values()) {
                String recordSql = "";
                String replaySql = "";
                switch (category) {
                    case SERVLET_ENTRANCE:
                        recordSql = String.format(SQL_TEMPLATE, "response as recordResponse",
                                "record_servlet_entrance", String.format("caseId = '%s'", idPair.getFirst()));
                        replaySql = String.format(SQL_TEMPLATE, "response as replayResponse",
                                "replay_servlet_entrance", String.format("replayId = '%s'", idPair.getSecond()));
                        break;
                    case DATABASE:
                        recordSql = String.format(SQL_TEMPLATE,
                                "dbname as recordDbname, parameters as recordParameters, sql as recordSql",
                                "record_database", String.format("caseId = '%s'", idPair.getFirst()));
                        replaySql = String.format(SQL_TEMPLATE,
                                "dbname as replayDbname, parameters as replayParameters, sql as replaySql",
                                "replay_database", String.format("replayId = '%s'", idPair.getSecond()));
                        break;
                    case DYNAMIC_CLASS:
                        recordSql = String.format(SQL_TEMPLATE,
                                "clazzName as recordClazzName, operation as recordOperation, operationKey as recordOperationKey",
                                "record_dynamic_class", String.format("caseId = '%s'", idPair.getFirst()));
                        replaySql = String.format(SQL_TEMPLATE,
                                "clazzName as replayClazzName,operation as replayOperation,operationKey as replayOperationKey",
                                "replay_dynamic_class", String.format("replayId = '%s'", idPair.getSecond()));
                        break;
                }
                if (StringUtil.isBlank(recordSql) || StringUtil.isBlank(replaySql)) {
                    continue;
                }
                recordList = StorageService.INSTANCE.query(recordSql);
                replayList = StorageService.INSTANCE.query(replaySql);
                if (CollectionUtil.isEmpty(recordList) && CollectionUtil.isEmpty(replayList)) {
                    continue;
                }
                int len = Math.max(recordList.size(), replayList.size());
                for (int i = 0; i < len; i++) {
                    Map<String, String> recordMap = new HashMap<>();
                    if (recordList.size() > i) {
                        recordList.get(i).forEach((key, val) -> {
                            key = StringUtils.substringAfter(key, MockDataType.RECORD.name()).toLowerCase();
                            recordMap.put(key, val);
                        });
                    }
                    Map<String, String> replayMap = new HashMap<>();
                    if (replayList.size() > i) {
                        replayList.get(i).forEach((key, val) -> {
                            key = StringUtils.substringAfter(key, MockDataType.REPLAY.name()).toLowerCase();
                            replayMap.put(key, val);
                        });
                    }
                    if (recordMap.size() > 1) {
                        recordJson = SerializeUtils.serialize(recordMap);
                    } else {
                        recordJson = recordMap.values().stream().findFirst().orElse(null);
                    }
                    if (replayMap.size() > 1) {
                        replayJson = SerializeUtils.serialize(replayMap);
                    } else {
                        replayJson = replayMap.values().stream().findFirst().orElse(null);
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

    public DiffMocker getDiffMocker(Pair<String, String> idPair, Pair<String, String> diffPair, MockerCategory category) {
        DiffMocker diffMocker = new DiffMocker(category);
        diffMocker.setCaseId(idPair.getFirst());
        diffMocker.setReplayId(idPair.getSecond());
        diffMocker.setRecordDiff(diffPair.getFirst());
        diffMocker.setReplayDiff(diffPair.getSecond());
        return diffMocker;
    }
}
