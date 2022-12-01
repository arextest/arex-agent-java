package io.arex.cli.storage;

import com.arextest.model.mock.MockCategoryType;
import com.arextest.model.mock.Mocker;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockerUtils;
import io.arex.foundation.serializer.SerializeUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class H2StorageServiceTest {
    static H2StorageService target = null;

    @BeforeAll
    static void setUp() {
        target = new H2StorageService();
        System.setProperty("arex.enable.debug", "true");
    }

    @AfterAll
    static void tearDown() {
        target = null;
        System.clearProperty("arex.enable.debug");
    }

    @Test
    @Order(1)
    void start() throws Exception {
        assertTrue(target.start());
    }

    @Test
    @Order(2)
    void save() {
        Mocker mocker = MockerUtils.createDatabase("db");
        String postJson = SerializeUtils.serialize(mocker);
        int count = target.save(mocker, postJson);
        assertEquals(1, count);
    }

    @Test
    @Order(3)
    void saveList() {
        DiffMocker mocker = new DiffMocker(MockCategoryType.DATABASE);
        int count = target.saveList(Collections.singletonList(mocker));
        assertEquals(1, count);
    }

    @Test
    @Order(4)
    void query() {
        Mocker mocker = MockerUtils.createDatabase("db");
        Mocker result = target.query(mocker);
        assertNotNull(result);
    }

    @Test
    @Order(5)
    void testQuery() {
        String sql = "SELECT * FROM MOCKER_INFO WHERE 1 = 1 AND REPLAYID = '' AND CATEGORYTYPE = 'Database' ORDER BY " +
                "CREATIONTIME DESC";
        List<Map<String, String>> result = target.query(sql);
        assertTrue(result.size() > 0);
    }

    @Test
    @Order(6)
    void queryList() {
        Mocker mocker = MockerUtils.createDatabase("db");
        List<Mocker> result = target.queryList(mocker, 10);
        assertTrue(result.size() > 0);
    }

    @Test
    @Order(7)
    void testQueryList() {
        DiffMocker mocker = new DiffMocker(MockCategoryType.DATABASE);
        List<DiffMocker> result = target.queryList(mocker);
        assertTrue(result.size() > 0);
    }

    @Test
    void startException() throws Exception {
        // trigger JdbcConnectionException
        System.setProperty("arex.storage.jdbc.url", "");
        assertFalse(target.start());
    }
}