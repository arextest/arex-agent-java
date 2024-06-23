package io.arex.inst.runtime.match.strategy;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.compare.model.eigen.EigenOptions;
import io.arex.agent.compare.model.eigen.EigenResult;
import io.arex.agent.compare.sdk.EigenSDK;
import io.arex.inst.runtime.match.MatchKeyFactory;
import io.arex.inst.runtime.match.MatchStrategyContext;
import io.arex.inst.runtime.model.MatchStrategyEnum;

import java.util.*;

public class EigenMatchStrategy extends AbstractMatchStrategy{

    private static final EigenSDK EIGEN_SDK = new EigenSDK();

    /**
     * search by eigen value of request
     */
    void process(MatchStrategyContext context) {
        context.setMatchStrategy(MatchStrategyEnum.EIGEN);
        Mocker replayMocker = context.getRequestMocker();
        List<Mocker> recordList = context.getRecordList();
        Mocker resultMocker = null;
        TreeMap<Integer, List<Mocker>> coincidePathMap = new TreeMap<>();
        // calculate all coincide path by eigen value
        calculateEigen(replayMocker);
        for (Mocker recordMocker : recordList) {
            if (recordMocker.isMatched()) {
                continue;
            }
            calculateEigen(recordMocker);
            int coincidePath = coincidePath(replayMocker.getEigenMap(), recordMocker.getEigenMap());
            coincidePathMap.computeIfAbsent(coincidePath, k -> new ArrayList<>()).add(recordMocker);
        }

        if (MapUtils.isEmpty(coincidePathMap)) {
            if (MockStrategyEnum.FIND_LAST == context.getMockStrategy()) {
                resultMocker = recordList.get(recordList.size() - 1);
            }
        } else {
            // get the max coincide path (first one in list, default order by creationTime)
            resultMocker = coincidePathMap.lastEntry().getValue().get(0);
        }
        setContextResult(context, resultMocker, "eigen match no result, all has been matched");
    }

    private void calculateEigen(Mocker mocker) {
        if (MapUtils.isNotEmpty(mocker.getEigenMap())) {
            return;
        }
        String eigenBody = MatchKeyFactory.INSTANCE.getEigenBody(mocker);
        if (StringUtil.isEmpty(eigenBody)) {
            return;
        }
        EigenOptions options = EigenOptions.options();
        options.putCategoryType(mocker.getCategoryType().getName());
        EigenResult eigenResult = EIGEN_SDK.calculateEigen(eigenBody, options);
        if (eigenResult == null) {
            return;
        }
        mocker.setEigenMap(eigenResult.getEigenMap());
    }

    private int coincidePath(Map<Integer, Long> replayEigenMap, Map<Integer, Long> recordEigenMap) {
        int row = 0;
        if (MapUtils.isEmpty(replayEigenMap) || MapUtils.isEmpty(recordEigenMap)) {
            return row;
        }

        for (Integer key : recordEigenMap.keySet()) {
            Long recordPathValue = recordEigenMap.get(key);
            Long replayPathValue = replayEigenMap.get(key);
            if (Objects.equals(recordPathValue, replayPathValue)) {
                row ++;
            }
        }
        return row;
    }
}
