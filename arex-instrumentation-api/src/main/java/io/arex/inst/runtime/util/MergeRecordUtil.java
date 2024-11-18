package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.util.sizeof.AgentSizeOf;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * merge record and replay util
 */
public class MergeRecordUtil {
    private static final AgentSizeOf agentSizeOf = AgentSizeOf.newInstance();

    private MergeRecordUtil() {}

    public static void mergeRecord(Mocker requestMocker) {
        List<List<Mocker>> mergeList = merge(requestMocker);
        for (List<Mocker> mergeRecords : mergeList) {
            MockUtils.executeRecord(mergeRecords);
        }
    }

    /**
     * merge duplicate mocker to queue and return merged result after reach batch count
     * @return if null mean no exceed merge max time limit not need to record or no merge
     */
    public static List<List<Mocker>> merge(Mocker mocker) {
        try {
            ArexContext context = ContextManager.currentContext();
            if (context == null) {
                return Collections.emptyList();
            }

            LinkedBlockingQueue<Mocker> mergeRecordQueue = context.getMergeRecordQueue();
            // offer queue to avoid block current thread
            if (!mergeRecordQueue.offer(mocker)) {
                // dynamic class not replay compare, log warn temporarily
                LogManager.warn("merge.record.fail", "queue is full");
                return Collections.emptyList();
            }
            int recordThreshold = Config.get().getInt(ArexConstants.MERGE_RECORD_THRESHOLD, ArexConstants.MERGE_RECORD_THRESHOLD_DEFAULT);
            if (mergeRecordQueue.size() < recordThreshold) {
                return Collections.emptyList();
            }

            List<Mocker> mergeList = new ArrayList<>();
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
    public static List<List<Mocker>> checkAndSplit(List<Mocker> mergeList) {
        mergeList = CollectionUtil.filterNull(mergeList);
        if (CollectionUtil.isEmpty(mergeList)) {
            return Collections.emptyList();
        }
        List<List<Mocker>> mergeTotalList = new ArrayList<>();
        // check memory size
        if (agentSizeOf.checkMemorySizeLimit(mergeList, ArexConstants.MEMORY_SIZE_5MB)) {
            mergeTotalList.add(mergeList);
        } else {
            // exceed size limit and split to multiple list
            mergeTotalList.addAll(split(mergeList));
        }
        return mergeTotalList;
    }

    /**
     * split strategy:
     * 1. split by config count: list[10] A -> list[5] B、list[5] C
     * 2. check memory size separate list
     * 3. if memory size not exceed limit return: list[[5]、[5]] R
     * 4. if memory size exceed limit, split to single-size list and return:
     * list[[1]、[1]、[1]、[1]、[1]、[1]、[1]、[1]、[1]、[1]] R
     */
    private static List<List<Mocker>> split(List<Mocker> mergeList) {
        List<List<Mocker>> splitTotalList = new ArrayList<>();
        // default split in half
        int splitCount = Config.get().getInt(ArexConstants.MERGE_SPLIT_COUNT, 2);
        List<List<Mocker>> splitResultList = CollectionUtil.split(mergeList, splitCount);
        for (List<Mocker> splitList : splitResultList) {
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

    private static void logBigSize(List<Mocker> mergeRecordList) {
        for (Mocker mocker : mergeRecordList) {
            LogManager.warn("merge.record.size.too.large",
                    StringUtil.format("please check following record data, if is dynamic class, suggest replace it, " +
                                    "category: %s, operationName: %s",
                            mocker.getCategoryType().getName(), mocker.getOperationName()));
        }
    }

    public static void recordRemain(ArexContext context) {
        if (context == null) {
            return;
        }
        LinkedBlockingQueue<Mocker> mergeRecordQueue = context.getMergeRecordQueue();
        if (mergeRecordQueue.isEmpty()) {
            return;
        }
        try {
            List<Mocker> mergeRecordList = new ArrayList<>();
            mergeRecordQueue.drainTo(mergeRecordList);
            List<List<Mocker>> splitList = checkAndSplit(mergeRecordList);
            for (List<Mocker> mergeRecords : splitList) {
                MockUtils.executeRecord(mergeRecords);
            }
        } catch (Exception e) {
            LogManager.warn("merge.record.remain.error", e);
        }
    }
}
