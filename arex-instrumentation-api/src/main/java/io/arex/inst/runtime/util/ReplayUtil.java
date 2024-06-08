package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.match.MatchKeyFactory;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.model.QueryAllMockerDTO;
import io.arex.inst.runtime.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        QueryAllMockerDTO requestMocker = new QueryAllMockerDTO();
        requestMocker.setRecordId(ContextManager.currentContext().getCaseId());
        requestMocker.setCategoryTypes(new String[]{MockCategoryType.DYNAMIC_CLASS.getName(), MockCategoryType.REDIS.getName()});

        List<Mocker> allMockerList = MockUtils.queryMockers(requestMocker);
        if (CollectionUtil.isEmpty(allMockerList)) {
            return;
        }

        filterMergeMocker(allMockerList);
        Map<Integer, List<Mocker>> cachedReplayMap = ContextManager.currentContext().getCachedReplayResultMap();
        buildReplayResultMap(allMockerList, cachedReplayMap);
        ascendingSortByCreationTime(cachedReplayMap);
    }

    /**
     * compatible with merge record, after batchSave publish can be removed
     */
    private static void filterMergeMocker(List<Mocker> allMockerList) {
        List<Mocker> splitMockerList = new ArrayList<>();
        Predicate<Mocker> filterMergeRecord = mocker -> ArexConstants.MERGE_RECORD_NAME.equals(mocker.getOperationName());
        for (Mocker mergeMocker : allMockerList) {
            if (!filterMergeRecord.test(mergeMocker)) {
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
        allMockerList.removeIf(filterMergeRecord);
        allMockerList.addAll(splitMockerList);
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
     *   fuzzyMatchKeyHash : [Mocker1, Mocker2, ...]
     * }
     * demo:
     * {
     *   1233213331 : [Mocker1],
     *   4545626535 : [Mocker2, Mocker3]
     *   8764987897 : [Mocker4, Mocker5, Mocker6]
     *   ...
     * }
     * </pre>
     */
    private static void buildReplayResultMap(List<Mocker> replayMockers, Map<Integer, List<Mocker>> cachedReplayResultMap) {
        for (Mocker replayMocker : replayMockers) {
            if (replayMocker == null) {
                continue;
            }
            // replay match need methodRequestTypeHash and methodSignatureHash
            if (replayMocker.getFuzzyMatchKey() == 0) {
                replayMocker.setFuzzyMatchKey(MatchKeyFactory.INSTANCE.generateFuzzyMatchKey(replayMocker));
            }
            if (replayMocker.getAccurateMatchKey() == 0) {
                replayMocker.setAccurateMatchKey(MatchKeyFactory.INSTANCE.generateAccurateMatchKey(replayMocker));
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
