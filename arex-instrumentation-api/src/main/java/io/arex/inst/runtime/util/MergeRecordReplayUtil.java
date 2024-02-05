package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.NumberUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeDTO;
import io.arex.inst.runtime.model.MergeReplayType;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.sizeof.AgentSizeOf;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * merge record and replay util
 */
public class MergeRecordReplayUtil {
    private static final AgentSizeOf agentSizeOf = AgentSizeOf.newInstance();

    private MergeRecordReplayUtil() {}

    public static void mergeRecord(Mocker requestMocker) {
        int methodSignatureHash = MockUtils.methodSignatureHash(requestMocker);
        MergeDTO mergeDTO = MergeDTO.of(requestMocker.getCategoryType().getName(),
                methodSignatureHash,
                requestMocker.getOperationName(),
                requestMocker.getTargetRequest().getBody(),
                requestMocker.getTargetResponse().getBody(),
                requestMocker.getTargetResponse().getType(),
                requestMocker.getTargetRequest().getAttributes(),
                requestMocker.getTargetResponse().getAttributes(),
                // save recordId prevent miss after clear context at async thread
                ContextManager.currentContext() != null ? ContextManager.currentContext().getCaseId() : StringUtil.EMPTY);
        mergeDTO.setCreationTime(requestMocker.getCreationTime());
        mergeDTO.setMethodRequestTypeHash(MockUtils.methodRequestTypeHash(requestMocker));
        List<List<MergeDTO>> mergeList = merge(mergeDTO);
        batchRecord(mergeList);
    }

    /**
     * merge duplicate mocker to queue and return merged result after reach batch count
     * @return if null mean no exceed merge max time limit not need to record or no merge
     */
    public static List<List<MergeDTO>> merge(MergeDTO mergeDTO) {
        try {
            ArexContext context = ContextManager.currentContext();
            if (context == null) {
                return Collections.emptyList();
            }

            LinkedBlockingQueue<MergeDTO> mergeRecordQueue = context.getMergeRecordQueue();
            // offer queue to avoid block current thread
            if (!mergeRecordQueue.offer(mergeDTO)) {
                // dynamic class not replay compare, log warn temporarily
                LogManager.warn("merge.record.fail", "queue is full");
                return Collections.emptyList();
            }
            int recordThreshold = Config.get().getInt(ArexConstants.MERGE_RECORD_THRESHOLD, ArexConstants.MERGE_RECORD_THRESHOLD_DEFAULT);
            if (mergeRecordQueue.size() < recordThreshold) {
                return Collections.emptyList();
            }

            List<MergeDTO> mergeList = new ArrayList<>();
            mergeRecordQueue.drainTo(mergeList, recordThreshold);
            return checkAndSplit(mergeList);
        } catch (Exception e) {
            LogManager.warn("merge.record.error", e);
            return Collections.emptyList();
        }
    }

    /**
     * check memory size or split to multiple list if exceed size limit
     */
    public static List<List<MergeDTO>> checkAndSplit(List<MergeDTO> mergeList) {
        mergeList = CollectionUtil.filterNull(mergeList);
        if (CollectionUtil.isEmpty(mergeList)) {
            return Collections.emptyList();
        }
        List<List<MergeDTO>> mergeTotalList = new ArrayList<>();
        Map<String, List<MergeDTO>> mergeRecordGroupMap = group(mergeList);
        for (Map.Entry<String, List<MergeDTO>> mergeRecordEntry : mergeRecordGroupMap.entrySet()) {
            // check memory size
            if (agentSizeOf.checkMemorySizeLimit(mergeRecordEntry.getValue(), ArexConstants.MEMORY_SIZE_5MB)) {
                mergeTotalList.add(mergeRecordEntry.getValue());
            } else {
                // exceed size limit and split to multiple list
                mergeTotalList.addAll(split(mergeRecordEntry.getValue()));
            }
        }
        return mergeTotalList;
    }

    /**
     * group by category(such as: dynamicClass、redis)
     */
    private static Map<String, List<MergeDTO>> group(List<MergeDTO> mergeList) {
        Map<String, List<MergeDTO>> mergeGroupMap = new HashMap<>();
        for (MergeDTO mergeDTO : mergeList) {
            if (mergeDTO == null) {
                continue;
            }
            String category = mergeDTO.getCategory();
            mergeGroupMap.computeIfAbsent(category, k -> new ArrayList<>()).add(mergeDTO);
        }
        return mergeGroupMap;
    }

    /**
     * split strategy:
     * 1. split by config count: list[10] A -> list[5] B、list[5] C
     * 2. check memory size separate list
     * 3. if memory size not exceed limit return: list[[5]、[5]] R
     * 4. if memory size exceed limit, split to single-size list and return:
     * list[[1]、[1]、[1]、[1]、[1]、[1]、[1]、[1]、[1]、[1]] R
     */
    private static List<List<MergeDTO>> split(List<MergeDTO> mergeList) {
        List<List<MergeDTO>> splitTotalList = new ArrayList<>();
        // default split in half
        int splitCount = Config.get().getInt(ArexConstants.MERGE_SPLIT_COUNT, 2);
        List<List<MergeDTO>> splitResultList = CollectionUtil.split(mergeList, splitCount);
        for (List<MergeDTO> splitList : splitResultList) {
            if (agentSizeOf.checkMemorySizeLimit(splitList, ArexConstants.MEMORY_SIZE_5MB)) {
                splitTotalList.add(splitList);
            } else {
                // split to single-size list
                splitTotalList.addAll(CollectionUtil.split(splitList, splitList.size()));
                logBigSize(splitList);
            }
        }
        LogManager.info("merge.record.split", StringUtil.format("original size: %s, split count: %s",
                mergeList.size() + "", splitTotalList.size() + ""));
        return splitTotalList;
    }

    private static void logBigSize(List<MergeDTO> mergeRecordList) {
        for (MergeDTO mergeDTO : mergeRecordList) {
            LogManager.warn("merge.record.size.too.large",
                    StringUtil.format("please check following record data, if is dynamic class, suggest replace it, " +
                                    "category: %s, operationName: %s",
                            mergeDTO.getCategory(), mergeDTO.getOperationName()));
        }
    }

    private static void batchRecord(List<List<MergeDTO>> splitList) {
        MockCategoryType categoryType;
        for (List<MergeDTO> mergeRecords : splitList) {
            categoryType = MockCategoryType.of(mergeRecords.get(0).getCategory());
            Mocker mergeMocker = MockUtils.create(categoryType, ArexConstants.MERGE_RECORD_NAME);
            mergeMocker.setRecordId(mergeRecords.get(0).getRecordId());
            mergeMocker.getTargetResponse().setBody(Serializer.serialize(mergeRecords));
            mergeMocker.getTargetResponse().setType(ArexConstants.MERGE_TYPE);
            MockUtils.executeRecord(mergeMocker);
        }
    }

    public static void recordRemain(ArexContext context) {
        if (context == null) {
            return;
        }
        LinkedBlockingQueue<MergeDTO> mergeRecordQueue = context.getMergeRecordQueue();
        if (mergeRecordQueue.isEmpty()) {
            return;
        }
        try {
            List<MergeDTO> mergeRecordList = new ArrayList<>();
            mergeRecordQueue.drainTo(mergeRecordList);
            List<List<MergeDTO>> splitList = checkAndSplit(mergeRecordList);
            if (CollectionUtil.isEmpty(splitList)) {
                return;
            }
            batchRecord(splitList);
        } catch (Exception e) {
            LogManager.warn("merge.record.remain.error", e);
        }
    }

    /**
     * init replay and cached replay result
     */
    public static void mergeReplay() {
        if (!ContextManager.needReplay()) {
            return;
        }
        Map<Integer, List<MergeDTO>> cachedReplayResultMap = ContextManager.currentContext().getCachedReplayResultMap();
        // if there are other types that need to be mergeReplay in the future, please add to MergeReplayType
        for (MergeReplayType mergeReplayType : MergeReplayType.values()) {
            Mocker mergeMocker = MockUtils.create(mergeReplayType.getMockCategoryType(), ArexConstants.MERGE_RECORD_NAME);
            int callReplayMax;
            int replayCount = 0;
            do {
                Mocker responseMocker = MockUtils.executeReplay(mergeMocker, MockStrategyEnum.OVER_BREAK);
                if (!MockUtils.checkResponseMocker(responseMocker)) {
                    break;
                }
                List<MergeDTO> mergeReplayList = Serializer.deserialize(responseMocker.getTargetResponse().getBody(),
                        ArexConstants.MERGE_TYPE);
                if (CollectionUtil.isEmpty(mergeReplayList)) {
                    LogManager.warn("merge.replay.fail", "mergeReplayList is empty");
                    break;
                }
                buildReplayResultMap(mergeReplayList, cachedReplayResultMap);
                replayCount ++;
                callReplayMax = getCallReplayMax(responseMocker);
            } while (replayCount < callReplayMax);
        }

        // ascending order
        sortByCreationTime(cachedReplayResultMap);
    }

    private static void buildReplayResultMap(List<MergeDTO> mergeReplayList, Map<Integer, List<MergeDTO>> cachedReplayResultMap) {
        for (int i = 0; i < mergeReplayList.size(); i++) {
            MergeDTO mergeReplayDTO = mergeReplayList.get(i);
            if (mergeReplayDTO == null) {
                continue;
            }
            cachedReplayResultMap.computeIfAbsent(mergeReplayDTO.getMethodRequestTypeHash(), k -> new ArrayList<>()).add(mergeReplayDTO);
        }
    }

    private static void sortByCreationTime(Map<Integer, List<MergeDTO>> cachedReplayResultMap) {
        for (List<MergeDTO> mergeReplayList : cachedReplayResultMap.values()) {
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

    private static int getCallReplayMax(Mocker replayMocker) {
        Mocker.Target targetResponse = replayMocker.getTargetResponse();
        int callReplayMax = NumberUtil.toInt(String.valueOf(targetResponse.getAttribute(ArexConstants.CALL_REPLAY_MAX)));
        int replayThreshold = Config.get().getInt(ArexConstants.MERGE_REPLAY_THRESHOLD, ArexConstants.MERGE_REPLAY_THRESHOLD_DEFAULT);
        return callReplayMax == 0 ? replayThreshold : callReplayMax;
    }
}
