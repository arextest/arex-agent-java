package io.arex.inst.runtime.model;

import io.arex.agent.bootstrap.model.ArexMocker;
import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.inst.runtime.serializer.Serializer;
import io.arex.inst.runtime.util.MockUtils;

public class InvalidCaseMocker {
    private InvalidCaseMocker() {
    }

    public static ArexMocker of(String postData) {
        final ArexMocker invalidMocker = Serializer.deserialize(postData, ArexMocker.class);
        if (invalidMocker == null) {
            return null;
        }
        return of(invalidMocker);
    }

    public static ArexMocker of(ArexMocker mocker) {
        final ArexMocker invalidMocker = new ArexMocker();
        invalidMocker.setCategoryType(mocker.getCategoryType());
        invalidMocker.setRecordId(mocker.getRecordId());
        invalidMocker.setAppId(mocker.getAppId());
        invalidMocker.setRecordEnvironment(mocker.getRecordEnvironment());
        invalidMocker.setCreationTime(mocker.getCreationTime());
        return invalidMocker;
    }

    public static ArexMocker of(MockCategoryType categoryType, String recordId, String appId) {
        final ArexMocker invalidMocker = new ArexMocker();
        invalidMocker.setRecordId(recordId);
        invalidMocker.setAppId(appId);
        invalidMocker.setCategoryType(categoryType);
        return invalidMocker;
    }
}
