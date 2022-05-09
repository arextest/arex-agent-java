package io.arex.cli.api.extension;

import io.arex.api.Mode;
import io.arex.api.mocker.Mocker;
import io.arex.foundation.internal.Pair;
import io.arex.cli.api.model.DiffMocker;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CliDataStorage
 */
public interface CliDataStorage extends Mode {
    Set<String> getSupportCategory();
    List<Pair<String, String>> queryList(DiffMocker mocker);
    int save(Mocker mocker, String postJson);
    int save(DiffMocker mocker);
    int saveList(List<DiffMocker> mockers);
    int batchSave(List<Object> mockers, String tableName, String mockerInfo);
    String query(Mocker mocker);
    List<Map<String, String>> query(String category, String recordId, String replayId);
    List<String> queryList(Mocker mocker, int count);
}
