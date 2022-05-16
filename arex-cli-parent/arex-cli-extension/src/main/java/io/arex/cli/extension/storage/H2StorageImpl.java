package io.arex.cli.extension.storage;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import io.arex.cli.api.extension.CliDataStorage;
import io.arex.api.mocker.Mocker;
import io.arex.api.storage.DataStorage;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.internal.Pair;
import io.arex.cli.api.model.DiffMocker;
import io.arex.foundation.serializer.SerializeUtils;
import io.arex.foundation.util.CollectionUtil;
import io.arex.foundation.util.StringUtil;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * h2 Service
 * @Date: Created in 2022/4/2
 * @Modified By:
 */
@AutoService({DataStorage.class, CliDataStorage.class})
public class H2StorageImpl implements DataStorage, CliDataStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2StorageImpl.class);

    private static Statement stmt = null;

    private Set<String> supportCategorySet = Sets.newHashSet("ServletEntrance", "Database");

    @Override
    public String getMode() {
        return "local";
    }

    @Override
    public void initial() {

        doInitial();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<String> saveRecordData(Mocker recordMocker) {
        String postJson = SerializeUtils.serialize(recordMocker);

        CompletableFuture<String> future = new CompletableFuture<>();
        int count = save(recordMocker, postJson);
        if (count > 0) {
            future.complete("local record success");
        } else {
            future.complete("local record fail");
        }
        return future;
    }

    @Override
    public Object queryReplayData(Mocker replayMocker) {
        String postJson = SerializeUtils.serialize(replayMocker);
        save(replayMocker, postJson);
        return query(replayMocker);
    }

    @Override
    public Set<String> getSupportCategory() {
        return supportCategorySet;
    }

    @Override
    public int save(Mocker mocker, String postJson){
        String tableName = buildTableName(mocker);
        List<Object> mockers = new ArrayList<>();
        mockers.add(mocker);
        return batchSave(mockers, tableName, postJson);
    }

    @Override
    public int save(DiffMocker mocker){
        List<DiffMocker> mockers = new ArrayList<>();
        mockers.add(mocker);
        return saveList(mockers);
    }

    @Override
    public int saveList(List<DiffMocker> mockers){
        if (CollectionUtil.isEmpty(mockers)) {
            return 0;
        }
        List<Object> mockerList = new ArrayList<>();
        String tableName = null;
        for (DiffMocker mocker : mockers) {
            tableName = buildTableName(mocker);
            mockerList.add(mocker);
        }
        return batchSave(mockerList, tableName, null);
    }

    @Override
    public int batchSave(List<Object> mockers, String tableName, String mockerInfo){
        int count = 0;
        try {
            String sql = H2SqlParser.generateInsertSql(mockers, tableName, mockerInfo);
            count = stmt.executeUpdate(sql);
        } catch (Throwable e) {
            LOGGER.warn("h2database batch save error", e);
        }
        return count;
    }

    @Override
    public String query(Mocker mocker){
        List<String> result = queryList(mocker, 0);
        return CollectionUtil.isNotEmpty(result) ? result.get(0) : null;
    }

    @Override
    public List<Map<String, String>> query(String category, String recordId, String replayId){
        List<Map<String, String>> result = new ArrayList<>();
        try {
            String sql = H2SqlParser.generateCompareSql(category, recordId, replayId);
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, String> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnLabel(i), rs.getString(i));
                }
                result.add(rowData);
            }
        } catch (Throwable e) {
            LOGGER.warn("h2database query error", e);
        }

        return result;
    }

    @Override
    public List<String> queryList(Mocker mocker, int count){
        String tableName = buildTableName(mocker);
        List<String> result = new ArrayList<>();
        try {
            String sql = H2SqlParser.generateSelectSql(mocker, tableName, count);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getString("MOCKERINFO"));
            }
        } catch (Throwable e) {
            LOGGER.warn("h2database query mocker list error", e);
        }
        return result;
    }

    @Override
    public List<Pair<String, String>> queryList(DiffMocker mocker) {
        List<Pair<String, String>> result = new ArrayList<>();
        try {
            String sql = H2SqlParser.generateSelectDiffSql(mocker);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Pair<String, String> pair = Pair.of(rs.getString("recordDiff"), rs.getString("replayDiff"));
                result.add(pair);
            }
        } catch (Throwable e) {
            LOGGER.warn("h2database query diff list error", e);
        }
        return result;
    }

    public boolean doInitial() {
        Server webServer = null;
        Connection connection = null;
        try {
            if (StringUtil.isNotEmpty(ConfigManager.INSTANCE.getStorageServiceWebPort())) {
                webServer = Server.createWebServer("-webPort", ConfigManager.INSTANCE.getStorageServiceWebPort());
                webServer.start();
            }
            JdbcConnectionPool cp = JdbcConnectionPool.create(ConfigManager.INSTANCE.getStorageServiceJdbcUrl(),
                    ConfigManager.INSTANCE.getStorageServiceUsername(), ConfigManager.INSTANCE.getStorageServicePassword());
            connection = cp.getConnection();
            stmt = connection.createStatement();
            Map<String, String> schemaMap = H2SqlParser.parseSchema();
            for (String schema : schemaMap.values()) {
                stmt.execute(schema);
            }
            return true;
        } catch (Exception e) {
            if (webServer != null) {
                webServer.stop();
            }
            try {
                if (connection != null) {
                    connection.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
            }
            LOGGER.warn("h2database start error", e);
        }
        return false;
    }

    private String buildTableName(Mocker mocker) {
        String prefix = StringUtil.isNotEmpty(mocker.getReplayId()) ? "REPLAY" : "RECORD";
        return String.format("%s_%s", prefix, mocker.getCategoryName());
    }

    private String buildTableName(DiffMocker mocker) {
        String prefix = StringUtil.isNotEmpty(mocker.getReplayId()) ? "REPLAY" : "RECORD";
        return String.format("%s_%s", prefix, mocker.getCategory());
    }
}
