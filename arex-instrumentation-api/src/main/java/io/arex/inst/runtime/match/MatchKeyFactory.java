package io.arex.inst.runtime.match;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.inst.runtime.match.key.DatabaseMatchKeyBuilderImpl;
import io.arex.inst.runtime.match.key.DefaultMatchKeyBuilderImpl;
import io.arex.inst.runtime.match.key.HttpClientMatchKeyBuilderImpl;
import io.arex.inst.runtime.match.key.MatchKeyBuilder;

import java.util.ArrayList;
import java.util.List;

public class MatchKeyFactory {
    public static final MatchKeyFactory INSTANCE = new MatchKeyFactory();

    private final List<MatchKeyBuilder> matchKeyBuilders;

    private MatchKeyFactory() {
        this.matchKeyBuilders = new ArrayList<>();
        matchKeyBuilders.add(new HttpClientMatchKeyBuilderImpl());
        matchKeyBuilders.add(new DatabaseMatchKeyBuilderImpl());

        // DefaultMatchKeyBuilderImpl must be the last one
        matchKeyBuilders.add(new DefaultMatchKeyBuilderImpl());
    }

    private MatchKeyBuilder find(MockCategoryType categoryType) {
        if (CollectionUtil.isNotEmpty(this.matchKeyBuilders)) {
            for (MatchKeyBuilder matchKeyBuilder : this.matchKeyBuilders) {
                if (matchKeyBuilder.isSupported(categoryType)) {
                    return matchKeyBuilder;
                }
            }
        }
        return null;
    }

    public int getFuzzyMatchKey(Mocker mocker) {
        MatchKeyBuilder matchKeyBuilder = find(mocker.getCategoryType());
        if (matchKeyBuilder == null) {
            return 0;
        }
        return matchKeyBuilder.getFuzzyMatchKey(mocker);
    }

    public int getAccurateMatchKey(Mocker mocker) {
        MatchKeyBuilder matchKeyBuilder = find(mocker.getCategoryType());
        if (matchKeyBuilder == null) {
            return 0;
        }
        return matchKeyBuilder.getAccurateMatchKey(mocker);
    }

    public String getEigenBody(Mocker mocker) {
        MatchKeyBuilder matchKeyBuilder = find(mocker.getCategoryType());
        if (matchKeyBuilder == null) {
            return null;
        }
        return matchKeyBuilder.getEigenBody(mocker);
    }
}
