package io.arex.inst.runtime.util;

import io.arex.agent.bootstrap.model.Mocker;
import io.arex.inst.runtime.context.ArexContext;

import java.util.List;

/**
 * decouple, resolve cycle dependency
 */
public class MockManager {
    public static void mergeRecord(Mocker requestMocker) {
        MergeRecordUtil.mergeRecord(requestMocker);
    }

    public static void recordRemain(ArexContext context) {
        MergeRecordUtil.recordRemain(context);
    }

    public static void executeRecord(List<Mocker> mockerList) {
        MockUtils.executeRecord(mockerList);
    }

    public static void saveReplayRemainCompareRelation(ArexContext context) {
        ReplayUtil.saveRemainCompareResult(context);
    }
}
