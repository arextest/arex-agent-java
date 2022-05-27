package io.arex.cli.storage;

import com.google.auto.service.AutoService;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockerCategory;
import io.arex.foundation.services.StorageService;
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

/**
 * H2 Storage Service
 */
@AutoService(StorageService.class)
public class H2StorageService extends StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2StorageService.class);

    private static Statement stmt = null;

    public int save(AbstractMocker mocker, String postJson){
        String tableName = mocker.getMockDataType().name() + "_" + mocker.getCategory().name();
        List<Object> mockers = new ArrayList<>();
        mockers.add(mocker);
        return batchSave(mockers, tableName, postJson);
    }

    public int saveList(List<DiffMocker> mockers){
        if (CollectionUtil.isEmpty(mockers)) {
            return 0;
        }
        List<Object> mockerList = new ArrayList<>();
        String tableName = "";
        for (DiffMocker mocker : mockers) {
            tableName = "DIFF_RESULT";
            mockerList.add(mocker);
        }
        return batchSave(mockerList, tableName, null);
    }

    public int batchSave(List<Object> mockers, String tableName, String mockerInfo){
        int count = 0;
        try {
            String sql = io.arex.cli.storage.H2SqlParser.generateInsertSql(mockers, tableName, mockerInfo);
            count = stmt.executeUpdate(sql);
        } catch (Throwable e) {
            LOGGER.warn("h2database batch save error", e);
        }
        return count;
    }

    public String query(AbstractMocker mocker){
        List<String> result = queryList(mocker, 0);
        return CollectionUtil.isNotEmpty(result) ? result.get(0) : null;
    }

    public List<Map<String, String>> query(String sql){
        List<Map<String, String>> result = new ArrayList<>();
        try {
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

    public List<String> queryList(AbstractMocker mocker, int count){
        List<String> result = new ArrayList<>();
        try {
            String sql = io.arex.cli.storage.H2SqlParser.generateSelectSql(mocker, count);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getString("MOCKERINFO"));
            }
        } catch (Throwable e) {
            LOGGER.warn("h2database query mocker list error", e);
        }
        return result;
    }

    public List<DiffMocker> queryList(DiffMocker mocker) {
        List<DiffMocker> result = new ArrayList<>();
        try {
            String sql = io.arex.cli.storage.H2SqlParser.generateSelectDiffSql(mocker);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                DiffMocker diffMocker = new DiffMocker();
                diffMocker.setReplayId(rs.getString("replayId"));
                diffMocker.setCaseId(rs.getString("caseId"));
                diffMocker.setCategory(MockerCategory.valueOf(rs.getString("category")));
                diffMocker.setRecordDiff(rs.getString("recordDiff"));
                diffMocker.setReplayDiff(rs.getString("replayDiff"));
                result.add(diffMocker);
            }
        } catch (Throwable e) {
            LOGGER.warn("h2database query diff list error", e);
        }
        return result;
    }

    public boolean start() throws Exception {
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
            Map<String, String> schemaMap = io.arex.cli.storage.H2SqlParser.parseSchema();
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
}
