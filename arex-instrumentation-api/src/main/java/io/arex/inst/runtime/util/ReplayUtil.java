package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.thirdparty.util.CompressUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.match.MatchKeyFactory;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.model.QueryAllMockerDTO;
import io.arex.inst.runtime.model.ReplayCompareResultDTO;
import io.arex.inst.runtime.serializer.Serializer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

/**
 * ReplayUtil
 */
public class ReplayUtil {
    /**
     * init replay all mockers under case and cached replay result at context
     */
    public static void queryMockers() {
        if (!ContextManager.needReplay()) {
            return;
        }
        try {
            QueryAllMockerDTO requestMocker = new QueryAllMockerDTO();
            requestMocker.setRecordId(ContextManager.currentContext().getCaseId());
            requestMocker.setReplayId(ContextManager.currentContext().getReplayId());
            List<Mocker> recordMockerList = MockUtils.queryMockers(requestMocker);
            if (CollectionUtil.isEmpty(recordMockerList)) {
                return;
            }

            filterMocker(recordMockerList);
            Map<Integer, List<Mocker>> cachedReplayMap = ContextManager.currentContext().getCachedReplayResultMap();
            cachedReplayMap.clear();
            buildReplayResultMap(recordMockerList, cachedReplayMap);
            ascendingSortByCreationTime(cachedReplayMap);
        } catch (Exception e) {
            LogManager.warn("replay.allMocker", e);
        }
    }

    /**
     * compatible with merge record, after batchSave publish can be removed
     */
    private static void filterMocker(List<Mocker> allMockerList) {
        List<Mocker> splitMockerList = new ArrayList<>();
        Predicate<Mocker> filterMergeRecord = mocker -> ArexConstants.MERGE_RECORD_NAME.equals(mocker.getOperationName());
        for (Mocker mocker : allMockerList) {
            // decompress zstd data
            decompress(mocker);

            if (!filterMergeRecord.test(mocker)) {
                continue;
            }
            List<MergeDTO> mergeReplayList = Serializer.deserialize(mocker.getTargetResponse().getBody(), ArexConstants.MERGE_TYPE);
            if (CollectionUtil.isEmpty(mergeReplayList)) {
                continue;
            }
            splitMockerList.addAll(convertMergeMocker(mergeReplayList));
        }
        if (CollectionUtil.isEmpty(splitMockerList)) {
            return;
        }
        allMockerList.removeIf(filterMergeRecord);
        allMockerList.addAll(splitMockerList);
    }

    private static void decompress(Mocker mocker) {
        // decompress zstd data
        String originalRequest = mocker.getRequest();
        if (StringUtil.isNotEmpty(originalRequest)) {
            originalRequest = CompressUtil.zstdDecompress(Base64.getDecoder().decode(originalRequest));
            mocker.setTargetRequest(Serializer.deserialize(originalRequest, ArexConstants.MOCKER_TARGET_TYPE));
        }
        String originalResponse = mocker.getResponse();
        if (StringUtil.isNotEmpty(originalResponse)) {
            originalResponse = CompressUtil.zstdDecompress(Base64.getDecoder().decode(originalResponse));
            mocker.setTargetResponse(Serializer.deserialize(originalResponse, ArexConstants.MOCKER_TARGET_TYPE));
        }
    }

    private static List<Mocker> convertMergeMocker(List<MergeDTO> mergeReplayList) {
        List<Mocker> convertMockerList = new ArrayList<>();
        for (MergeDTO mergeDTO : mergeReplayList) {
            if (mergeDTO == null || (!MockCategoryType.DYNAMIC_CLASS.getName().equals(mergeDTO.getCategory())
                    && !MockCategoryType.REDIS.getName().equals(mergeDTO.getCategory()))) {
                continue;
            }
            ArexMocker mocker = MockUtils.create(MockCategoryType.of(mergeDTO.getCategory()), mergeDTO.getOperationName());
            mocker.setFuzzyMatchKey(mergeDTO.getMethodRequestTypeHash());
            mocker.setAccurateMatchKey(mergeDTO.getMethodSignatureHash());
            mocker.setCreationTime(mergeDTO.getCreationTime());
            mocker.getTargetRequest().setBody(mergeDTO.getRequest());
            mocker.getTargetRequest().setAttributes(mergeDTO.getRequestAttributes());
            mocker.getTargetResponse().setBody(mergeDTO.getResponse());
            mocker.getTargetResponse().setAttributes(mergeDTO.getResponseAttributes());
            mocker.getTargetResponse().setType(mergeDTO.getResponseType());
            convertMockerList.add(mocker);
        }
        return convertMockerList;
    }

    /**
     * <pre>
     * format:
     * {
     *   fuzzyMatchKeyHash : [Mockers]
     * }
     *
     * demo:
     * {
     *   1233213331 : [
     *                  {
     *                  "accurateMatchKey": 3454562343,
     *                  "categoryType": "Httpclient",
     *                  "operationName": "/order/query",
     *                  "targetRequest": "...",
     *                  "targetResponse": "...",
     *                  ...
     *                  }
     *               ]
     *   4545626535 : [
     *                  {
     *                  "accurateMatchKey": 6534247741,
     *                  "categoryType": "Database",
     *                  "operationName": "query",
     *                  "targetRequest": "...",
     *                  "targetResponse": "...",
     *                  ...
     *                  },
     *                  {
     *                  "accurateMatchKey": 9866734220,
     *                  "categoryType": "Database",
     *                  "operationName": "update",
     *                  "targetRequest": "...",
     *                  "targetResponse": "...",
     *                  ...
     *                  }
     *               ]
     *   ...
     * }
     * </pre>
     */
    private static void buildReplayResultMap(List<Mocker> recordMockerList, Map<Integer, List<Mocker>> cachedReplayResultMap) {
        for (Mocker recordMocker : recordMockerList) {
            if (recordMocker == null) {
                continue;
            }
            // replay match need methodRequestTypeHash and methodSignatureHash
            if (recordMocker.getFuzzyMatchKey() == 0) {
                recordMocker.setFuzzyMatchKey(MatchKeyFactory.INSTANCE.getFuzzyMatchKey(recordMocker));
            }
            if (recordMocker.getAccurateMatchKey() == 0) {
                recordMocker.setAccurateMatchKey(MatchKeyFactory.INSTANCE.getAccurateMatchKey(recordMocker));
            }
            // eigen will be calculated in agent
            recordMocker.setEigenMap(null);
            cachedReplayResultMap.computeIfAbsent(recordMocker.getFuzzyMatchKey(), k -> new ArrayList<>()).add(recordMocker);
        }
    }

    private static void ascendingSortByCreationTime(Map<Integer, List<Mocker>> cachedReplayResultMap) {
        for (List<Mocker> mergeReplayList : cachedReplayResultMap.values()) {
            if (mergeReplayList.size() == 1) {
                continue;
            }
            mergeReplayList.sort((o1, o2) -> {
                if (o1.getCreationTime() == o2.getCreationTime()) {
                    return 0;
                }
                return o1.getCreationTime() - o2.getCreationTime() > 0 ? 1 : -1;
            });
        }
    }

    public static void saveReplayCompareResult() {
        ArexContext context = ContextManager.currentContext();
        if (context == null || !context.isReplay()) {
            return;
        }
        LinkedBlockingQueue<ReplayCompareResultDTO> replayCompareResultQueue = context.getReplayCompareResultQueue();
        if (replayCompareResultQueue.isEmpty()) {
            return;
        }
        List<ReplayCompareResultDTO> replayCompareList = new ArrayList<>();
        replayCompareResultQueue.drainTo(replayCompareList);
        MockUtils.saveReplayCompareResult(context, replayCompareList);

        Map<Integer, List<Mocker>> cachedReplayResultMap = context.getCachedReplayResultMap();
        StringBuilder message = new StringBuilder();
        for (List<Mocker> cachedReplayList : cachedReplayResultMap.values()) {
            for (Mocker cachedMocker : cachedReplayList) {
                message.append(StringUtil.format("matched: %s, detail: %s %n",
                        String.valueOf(cachedMocker.isMatched()), cachedMocker.logBuilder().toString()));
            }
        }
        LogManager.info("CachedReplayResultMap", message.toString());
    }

    /**
     * call missing 类型只能在最后统计一次，保证所有的回放匹配（包括异步的）都匹配结束，这样统计的call missing才是准确的
     * 如果在主接口就统计call missing，可能会有异步接口还未匹配，导致call missing统计不准确，
     * 虽然现在是未匹配过的状态，但等异步匹配之后可能变成了匹配中的状态了，所以要放到最后统计
     */
    public static void saveRemainCompareResult(ArexContext context) {
        if (context == null) {
            return;
        }
        LinkedBlockingQueue<ReplayCompareResultDTO> replayCompareResultQueue = context.getReplayCompareResultQueue();
        // find unmatched mockers (call missing)
        Map<Integer, List<Mocker>> cachedReplayResultMap = context.getCachedReplayResultMap();
        for (List<Mocker> cachedReplayList : cachedReplayResultMap.values()) {
            for (Mocker cachedMocker : cachedReplayList) {
                if (cachedMocker.isMatched() || cachedMocker.getCategoryType().isSkipComparison()) {
                    continue;
                }
                cachedMocker.setAppId(System.getProperty("arex.service.name"));
                cachedMocker.setReplayId(context.getReplayId());
                String recordMsg = getCompareMessage(cachedMocker);
                ReplayCompareResultDTO callMissingDTO = convertCompareResult(cachedMocker, recordMsg,
                        null, cachedMocker.getCreationTime(), Long.MAX_VALUE, false);
                replayCompareResultQueue.offer(callMissingDTO);
                // log call missing
                String message = StringUtil.format("%s %n%s",
                        "match fail, reason: call missing", cachedMocker.logBuilder().toString());
                if (Config.get().isEnableDebug()) {
                    message += StringUtil.format("%ncall missing mocker: %s", Serializer.serialize(cachedMocker));
                }
                LogManager.info(context, ArexConstants.MATCH_LOG_TITLE, message);
            }
        }
        if (replayCompareResultQueue.isEmpty()) {
            return;
        }
        List<ReplayCompareResultDTO> replayCompareList = new ArrayList<>();
        replayCompareResultQueue.drainTo(replayCompareList);
        MockUtils.saveReplayCompareResult(context, replayCompareList);
    }

    public static ReplayCompareResultDTO convertCompareResult(Mocker replayMocker, String recordMsg, String replayMsg,
                                                              long recordTime, long replayTime, boolean sameMsg) {
        ReplayCompareResultDTO compareResult = new ReplayCompareResultDTO();
        compareResult.setAppId(replayMocker.getAppId());
        compareResult.setCategoryType(replayMocker.getCategoryType());
        compareResult.setOperationName(replayMocker.getOperationName());
        compareResult.setRecordId(replayMocker.getRecordId());
        compareResult.setReplayId(replayMocker.getReplayId());
        compareResult.setRecordMessage(recordMsg);
        compareResult.setReplayMessage(replayMsg);
        compareResult.setRecordTime(recordTime);
        compareResult.setReplayTime(replayTime);
        compareResult.setSameMessage(sameMsg);
        return compareResult;
    }

    public static String getCompareMessage(Mocker mocker) {
        String compareMessage = mocker.getTargetRequest().getBody();
        if (mocker.getCategoryType().isEntryPoint()) {
            compareMessage = mocker.getTargetResponse().getBody();
        } else if (MockCategoryType.DATABASE.getName().equals(mocker.getCategoryType().getName())
                || MockCategoryType.MESSAGE_PRODUCER.getName().equals(mocker.getCategoryType().getName())) {
            compareMessage = Serializer.serialize(mocker.getTargetRequest());
        }
        return compareMessage;
    }
}
