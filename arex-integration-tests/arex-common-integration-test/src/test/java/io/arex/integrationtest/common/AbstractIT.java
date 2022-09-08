package io.arex.integrationtest.common;

import java.io.File;
import java.io.FileFilter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractIT {

    public static String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    static {
        System.setProperty("arex.enable.debug", "true");
        System.setProperty("arex.service.name", "test-app");
        System.setProperty("arex.storage.mode", "local");
        System.setProperty("arex.storage.jdbc.url", jdbcUrl);
    }

    public static List<String> queryDB(String sql) throws Exception {
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:testdb", "arex", "123");
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery(sql);
        List<String> results = new ArrayList<>();
        while (rs.next()) {
            String recordJson = URLDecoder.decode(rs.getString("jsonData"), StandardCharsets.UTF_8.name());
            results.add(recordJson);
        }
        return results;
    }

    public static void clearSystemProperties() {
        System.clearProperty("arex.enable.debug");
        System.clearProperty("arex.service.name");
        System.clearProperty("arex.storage.mode");
        System.clearProperty("arex.storage.jdbc.url");
    }

    public static String getAgentJarPath(String jarPrefix) {
        File file = getAgentJar(jarPrefix);
        return file.getAbsolutePath();
    }

    public static String getAgentJarName(String jarPrefix) {
        File file = getAgentJar(jarPrefix);
        return file.getName();
    }

    public static File getAgentJar(String jarPrefix) {
        return getJarFile("../../arex-agent-jar/", jarPrefix);
    }

    public static File getJarFile(String dir, String jarPrefix) {
        File buildDir = new File(dir);
        FileFilter fileFilter = file -> file.getName().matches(jarPrefix + "-\\d\\.\\d+\\.\\d+(\\.RC\\d+)?(-SNAPSHOT)?.jar");
        File[] jars = buildDir.listFiles(fileFilter);
        assertTrue(jars != null && jars.length > 0, jarPrefix + " jar not found. Execute mvn package to build the agent jar.");
        return jars[0];
    }

    /**
     * for local mode
     */
    public static String getLocalModeJar(String jarName) {
        Path path = Paths.get("../../arex-cli-parent", jarName, "target", jarName + ".jar");
        assertTrue(Files.exists(path), jarName + " jar not found. Execute mvn package to build the jar.");
        return path.toAbsolutePath().toString();
    }
}
