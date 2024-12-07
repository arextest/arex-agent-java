package io.arex.inst.runtime.match.strategy;

import io.arex.agent.bootstrap.model.MockStrategyEnum;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.MapUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.agent.compare.model.eigen.EigenOptions;
import io.arex.agent.compare.model.eigen.EigenResult;
import io.arex.agent.compare.sdk.EigenCalculateSDK;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.match.MatchKeyFactory;
import io.arex.inst.runtime.match.MatchStrategyContext;
import io.arex.inst.runtime.model.CompareConfigurationEntity;
import io.arex.inst.runtime.model.CompareConfigurationEntity.ConfigComparisonExclusionsEntity;
import io.arex.inst.runtime.model.MatchStrategyEnum;

import java.util.*;

public class EigenMatchStrategy extends AbstractMatchStrategy{

    private static final EigenCalculateSDK EIGEN_SDK = new EigenCalculateSDK();

    /**
     * search by eigen value of request
     */
    public void process(MatchStrategyContext context) {
        context.setMatchStrategy(MatchStrategyEnum.EIGEN);
        Mocker replayMocker = context.getRequestMocker();
        List<Mocker> recordList = context.getRecordList();
        Mocker resultMocker = null;
        TreeMap<Integer, List<Mocker>> coincidePathMap = new TreeMap<>();
        // calculate all coincide path by eigen value
        EigenOptions eigenOptions = getEigenOptions(replayMocker);
        calculateEigen(replayMocker, eigenOptions);
        for (Mocker recordMocker : recordList) {
            if (recordMocker.isMatched()) {
                continue;
            }
            calculateEigen(recordMocker, eigenOptions);
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
        setContextResult(context, resultMocker, "new call, eigen match no result, all has been used");
    }

    private void calculateEigen(Mocker mocker, EigenOptions options) {
        if (MapUtils.isNotEmpty(mocker.getEigenMap())) {
            return;
        }
        String eigenBody = MatchKeyFactory.INSTANCE.getEigenBody(mocker);
        if (StringUtil.isEmpty(eigenBody)) {
            return;
        }

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

    private EigenOptions getEigenOptions(Mocker mocker) {
        EigenOptions options = new EigenOptions();
        options.setCategoryType(mocker.getCategoryType().getName());
        CompareConfigurationEntity compareConfiguration = Config.get().getCompareConfiguration();
        if (compareConfiguration == null) {
            return options;
        }
        Set<List<String>> exclusions = new HashSet<>();
        List<ConfigComparisonExclusionsEntity> comparisonExclusions = compareConfiguration.getComparisonExclusions();
        if (CollectionUtil.isNotEmpty(comparisonExclusions)) {
            for (ConfigComparisonExclusionsEntity exclusion : comparisonExclusions) {
                if (exclusion == null) {
                    continue;
                }
                if (mocker.getCategoryType().getName().equalsIgnoreCase(exclusion.getCategoryType())
                        && mocker.getOperationName().equalsIgnoreCase(exclusion.getOperationName())) {
                    exclusions.addAll(exclusion.getExclusionList());
                }
            }
        }
        options.setIgnoreNodes(compareConfiguration.getIgnoreNodeSet());
        options.setExclusions(CollectionUtil.isNotEmpty(exclusions) ? exclusions : compareConfiguration.getGlobalExclusionList());
        return options;
    }
}
