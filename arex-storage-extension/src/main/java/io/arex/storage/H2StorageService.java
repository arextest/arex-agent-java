package io.arex.storage;

import com.google.auto.service.AutoService;
import io.arex.foundation.config.ConfigManager;
import io.arex.foundation.internal.Pair;
import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockDataType;
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
 * h2 Service
 * @Date: Created in 2022/4/2
 * @Modified By:
 */
@AutoService(StorageService.class)
public class H2StorageService extends StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2StorageService.class);

    private static Statement stmt = null;

    public int save(AbstractMocker mocker, String postJson){
        int count = 0;
        try {
            String tableName = mocker.getMockDataType().name() + "_" + mocker.getCategory().name();
            String sql = H2SqlParser.generateInsertSql(mocker, tableName, postJson);
            count = stmt.executeUpdate(sql);
        } catch (Throwable e) {
            LOGGER.warn("h2database save mocker error", e);
        }
        return count;
    }

    public int save(DiffMocker mocker){
        int count = 0;
        try {
            String tableName = "DIFF_" + mocker.getCategory().name();
            String sql = H2SqlParser.generateInsertSql(mocker, tableName, null);
            count = stmt.executeUpdate(sql);
        } catch (Throwable e) {
            LOGGER.warn("h2database save diff error", e);
        }
        return count;
    }

    public String query(AbstractMocker mocker, MockDataType type){
        List<String> result = queryList(mocker, type, 0);
        return CollectionUtil.isNotEmpty(result) ? result.get(0) : null;
    }

    public List<Map<String, String>> query(MockerCategory category, String recordId, String replayId){
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

    public List<String> queryList(AbstractMocker mocker, MockDataType type, int count){
        List<String> result = new ArrayList<>();
        try {
            String sql = H2SqlParser.generateSelectSql(mocker, type, count);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result.add(rs.getString("MOCKERINFO"));
            }
        } catch (Throwable e) {
            LOGGER.warn("h2database query mocker list error", e);
        }
        return result;
    }

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

    public boolean start() throws Exception {
        Server tcpServer = null;
        Server webServer = null;
        Connection connection = null;
        try {
            if (ConfigManager.INSTANCE.isLocalStorage()) {
                tcpServer = Server.createTcpServer("-ifNotExists");
                tcpServer.start();
                if (StringUtil.isNotEmpty(ConfigManager.INSTANCE.getStorageServiceWebPort())) {
                    webServer = Server.createWebServer("-webPort", ConfigManager.INSTANCE.getStorageServiceWebPort());
                    webServer.start();
                }
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
            if (tcpServer != null) {
                tcpServer.stop();
            }
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
            throw e;
        }
    }
}
