package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.MergeResultDTO;
import io.arex.inst.runtime.util.sizeof.AgentSizeOf;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class MergeSplitUtil {
    private static final long MERGE_MAX_SIZE_5MB = 5 * 1024L * 1024L;

    /**
     * merge duplicate mocker to queue and return merged result After reach batch count
     * @return if null mean no exceed merge max time limit not need to record or no merge
     */
    public static List<List<MergeResultDTO>> merge(Mocker mocker) {
        try {
            if (!Config.get().getBoolean(ArexConstants.MERGE_RECORD_ENABLE, true)) {
                return null;
            }

            ArexContext context = ContextManager.currentContext();
            if (context == null) {
                return null;
            }
            LinkedBlockingQueue<MergeResultDTO> mergeRecordQueue = context.getMergeRecordQueue();
            MergeResultDTO mergeResultDTO = (MergeResultDTO) mocker.getTargetRequest().getAttribute(ArexConstants.MERGE_RECORD_KEY);
            // offer queue to avoid block current thread
            if (!mergeRecordQueue.offer(mergeResultDTO)) {
                // dynamic class not replay compare, log warn temporarily
                LogManager.warn("merge dynamicClass fail", "queue is full");
                return null;
            }

            int recordThreshold = Config.get().getInt(ArexConstants.MERGE_RECORD_THRESHOLD, ArexConstants.MERGE_RECORD_THRESHOLD_DEFAULT);
            // if main entry record has ended, it means still async thread record(not merge and direct record)
            if (mergeRecordQueue.size() < recordThreshold && !context.isMainEntryEnd()) {
                return null;
            }
            List<MergeResultDTO> mergeRecordList = new ArrayList<>();
            for (int i = 0; i < recordThreshold; i++) {
                MergeResultDTO mergeResult = mergeRecordQueue.poll();
                if (mergeResult != null) {
                    mergeRecordList.add(mergeResult);
                }
            }

            return checkAndSplit(mergeRecordList);
        } catch (Throwable e) {
            LogManager.warn("MergeUtil merge error", e);
            return null;
        }
    }

    private static List<List<MergeResultDTO>> checkAndSplit(List<MergeResultDTO> mergeRecordList) {
        mergeRecordList = CollectionUtil.filterNull(mergeRecordList);
        if (CollectionUtil.isEmpty(mergeRecordList)) {
            return null;
        }
        List<List<MergeResultDTO>> mergeTotalList = new ArrayList<>();
        Map<String, List<MergeResultDTO>> mergeRecordGroupMap = group(mergeRecordList);
        for (Map.Entry<String, List<MergeResultDTO>> mergeRecordEntry : mergeRecordGroupMap.entrySet()) {
            // check memory size and split to multiple list
            if (!checkMemorySizeLimit(mergeRecordEntry.getValue())) {
                mergeTotalList.addAll(split(mergeRecordEntry.getValue()));
            } else {
                mergeTotalList.add(mergeRecordEntry.getValue());
            }
        }
        return mergeTotalList;
    }

    /**
     * group by category(such as: dynamicClass、redis)
     */
    private static Map<String, List<MergeResultDTO>> group(List<MergeResultDTO> mergeRecordList) {
        Map<String, List<MergeResultDTO>> mergeRecordGroupMap = new HashMap<>();
        for (MergeResultDTO mergeResultDTO : mergeRecordList) {
            if (mergeResultDTO == null) {
                continue;
            }
            String category = mergeResultDTO.getCategory();
            mergeRecordGroupMap.computeIfAbsent(category, k -> new ArrayList<>()).add(mergeResultDTO);
        }
        return mergeRecordGroupMap;
    }

    private static boolean checkMemorySizeLimit(Object obj) {
        if (!Config.get().getBoolean(ArexConstants.MERGE_MEMORY_CHECK, true)) {
            return true;
        }
        long start = System.currentTimeMillis();
        AgentSizeOf agentSizeOf = AgentSizeOf.newInstance();
        long memorySize = agentSizeOf.deepSizeOf(obj);
        long cost = System.currentTimeMillis() - start;
        if (cost > 10) { // longer cost mean larger memory
            LogManager.warn("checkMemorySizeLimit", StringUtil.format("size: %s, cost: %s",
                    AgentSizeOf.humanReadableUnits(memorySize), String.valueOf(cost)));
        }
        return memorySize < MERGE_MAX_SIZE_5MB;
    }

    /**
     * split strategy:
     * 1. split by config count: list[10] A -> list[5] B、list[5] C
     * 2. check memory size separate list
     * 3. if memory size not exceed limit return: list[[5]、[5]] R
     * 4. if memory size exceed limit, split to single-size list and return:
     * list[[1]、[1]、[1]、[1]、[1]、[1]、[1]、[1]、[1]、[1]] R
     */
    private static List<List<MergeResultDTO>> split(List<MergeResultDTO> mergeRecordList) {
        List<List<MergeResultDTO>> splitTotalList = new ArrayList<>();
        // default split in half
        int splitCount = Config.get().getInt(ArexConstants.MERGE_SPLIT_COUNT, 2);
        List<List<MergeResultDTO>> splitResultList = CollectionUtil.split(mergeRecordList, splitCount);
        for (List<MergeResultDTO> splitList : splitResultList) {
            if (checkMemorySizeLimit(splitList)) {
                splitTotalList.add(splitList);
            } else {
                // split to single-size list
                splitTotalList.addAll(CollectionUtil.split(splitList, splitList.size()));
            }
        }
        LogManager.info("split merged record", StringUtil.format("original size: %s, split count: %s",
                mergeRecordList.size() + "", splitTotalList.size() + ""));
        return splitTotalList;
    }

    /**
     * merge all that did not exceed the threshold at the end of the request
     */
    public static List<List<MergeResultDTO>> mergeRemain() {
        try {
            ArexContext context = ContextManager.currentContext();
            if (context == null) {
                return null;
            }
            LinkedBlockingQueue<MergeResultDTO> mergeRecordQueue = context.getMergeRecordQueue();
            if (mergeRecordQueue.size() == 0) {
                return null;
            }
            MergeResultDTO[] mergeRecordArray = mergeRecordQueue.toArray(new MergeResultDTO[0]);
            mergeRecordQueue.clear();
            List<MergeResultDTO> mergeRecordList = Arrays.asList(mergeRecordArray);
            return checkAndSplit(mergeRecordList);
        } catch (Throwable e) {
            LogManager.warn("MergeUtil mergeRemain error", e);
            return null;
        }
    }
}
