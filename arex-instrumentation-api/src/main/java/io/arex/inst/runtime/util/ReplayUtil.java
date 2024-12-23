package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.model.QueryAllMockerDTO;
import io.arex.inst.runtime.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Replay all mockers under case
 * (called once after arexContext init)
 */
public class ReplayUtil {

    private static final Predicate<Mocker> FILTER_MERGE_RECORD = mocker ->
            ArexConstants.MERGE_RECORD_NAME.equals(mocker.getOperationName());

    private static final Predicate<MergeDTO> FILTER_MERGE_TYPE = mergeDTO ->
            !MockCategoryType.DYNAMIC_CLASS.getName().equals(mergeDTO.getCategory())
            && !MockCategoryType.REDIS.getName().equals(mergeDTO.getCategory());

    /**
     * init replay all mocker under case and cached replay result
     */
    public static void queryMockers() {
        if (!ContextManager.needReplay()) {
            return;
        }
        try {
            QueryAllMockerDTO requestMocker = new QueryAllMockerDTO();
            requestMocker.setRecordId(ContextManager.currentContext().getCaseId());
            requestMocker.setReplayId(ContextManager.currentContext().getReplayId());
            List<Mocker> allMockerList = MockUtils.queryMockers(requestMocker);
            if (CollectionUtil.isEmpty(allMockerList)) {
                return;
            }

            filterMergeMocker(allMockerList);

            Map<Integer, List<Mocker>> cachedReplayResultMap = ContextManager.currentContext().getCachedReplayResultMap();
            cachedReplayResultMap.clear();
            buildReplayResultMap(allMockerList, cachedReplayResultMap);

            ascendingSortByCreationTime(cachedReplayResultMap);
        } catch (Exception e) {
            LogManager.warn("replay.all.mocker", e);
        }
    }

    /**
     * compatible with merge record, after batchSave published can be removed
     */
    private static void filterMergeMocker(List<Mocker> allMockerList) {
        List<Mocker> splitMockerList = new ArrayList<>();
        for (Mocker mergeMocker : allMockerList) {
            if (!FILTER_MERGE_RECORD.test(mergeMocker)) {
                continue;
            }
            List<MergeDTO> mergeReplayList = Serializer.deserialize(mergeMocker.getTargetResponse().getBody(), ArexConstants.MERGE_TYPE);
            if (CollectionUtil.isEmpty(mergeReplayList)) {
                continue;
            }
            splitMockerList.addAll(convertMergeMocker(mergeReplayList));
        }
        if (CollectionUtil.isEmpty(splitMockerList)) {
            return;
        }
        allMockerList.removeIf(FILTER_MERGE_RECORD);
        allMockerList.addAll(splitMockerList);
    }

    private static List<Mocker> convertMergeMocker(List<MergeDTO> mergeReplayList) {
        List<Mocker> convertMockerList = new ArrayList<>();
        for (MergeDTO mergeDTO : mergeReplayList) {
            if (mergeDTO == null || FILTER_MERGE_TYPE.test(mergeDTO)) {
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

    private static void buildReplayResultMap(List<Mocker> replayMockers, Map<Integer, List<Mocker>> cachedReplayResultMap) {
        for (Mocker replayMocker : replayMockers) {
            if (replayMocker == null) {
                continue;
            }
            // replay match need methodRequestTypeHash and methodSignatureHash
            if (replayMocker.getFuzzyMatchKey() == 0) {
                replayMocker.setFuzzyMatchKey(MockUtils.methodRequestTypeHash(replayMocker));
            }
            if (replayMocker.getAccurateMatchKey() == 0) {
                replayMocker.setAccurateMatchKey(MockUtils.methodSignatureHash(replayMocker));
            }
            cachedReplayResultMap.computeIfAbsent(replayMocker.getFuzzyMatchKey(), k -> new ArrayList<>()).add(replayMocker);
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
}
