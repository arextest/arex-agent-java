package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.model.QueryAllMockerDTO;
import io.arex.inst.runtime.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * merge record and replay util
 */
public class ReplayUtil {

    /**
     * init replay all mocker under case and cached replay result
     */
    public static void replayAllMocker() {
        if (!ContextManager.needReplay()) {
            return;
        }
        QueryAllMockerDTO requestMocker = new QueryAllMockerDTO();
        requestMocker.setRecordId(ContextManager.currentContext().getCaseId());
        requestMocker.setCategoryTypes(new String[]{MockCategoryType.DYNAMIC_CLASS.getName(), MockCategoryType.REDIS.getName()});

        List<Mocker> allMockerList = MockUtils.replayAllMocker(requestMocker);
        if (CollectionUtil.isEmpty(allMockerList)) {
            return;
        }

        filterMergeMocker(allMockerList);

        Map<Integer, List<Mocker>> cachedReplayResultMap = ContextManager.currentContext().getCachedReplayResultMap();

        buildReplayResultMap(allMockerList, cachedReplayResultMap);

        ascendingSortByCreationTime(cachedReplayResultMap);
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
            mocker.setMethodRequestTypeHash(mergeDTO.getMethodRequestTypeHash());
            mocker.setMethodSignatureHash(mergeDTO.getMethodSignatureHash());
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
            if (replayMocker.getMethodRequestTypeHash() == 0) {
                replayMocker.setMethodRequestTypeHash(MockUtils.methodRequestTypeHash(replayMocker));
            }
            if (replayMocker.getMethodSignatureHash() == 0) {
                replayMocker.setMethodSignatureHash(MockUtils.methodSignatureHash(replayMocker));
            }
            cachedReplayResultMap.computeIfAbsent(replayMocker.getMethodRequestTypeHash(), k -> new ArrayList<>()).add(replayMocker);
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
