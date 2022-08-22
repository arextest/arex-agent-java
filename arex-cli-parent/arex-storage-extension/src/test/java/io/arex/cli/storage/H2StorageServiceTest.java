package io.arex.cli.storage;

import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.DatabaseMocker;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockerCategory;
import io.arex.foundation.serializer.SerializeUtils;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class H2StorageServiceTest {
    static H2StorageService target = null;

    @BeforeAll
    static void setUp() {
        target = new H2StorageService();
    }

    @AfterAll
    static void tearDown() {
        target = null;
    }

    @Test
    @Order(1)
    void start() throws Exception {
        assertTrue(target.start());
    }

    @Test
    @Order(2)
    void save() {
        AbstractMocker mocker = new DatabaseMocker();
        String postJson = SerializeUtils.serialize(mocker);
        int count = target.save(mocker, postJson);
        assertEquals(1, count);
    }

    @Test
    @Order(3)
    void saveList() {
        DiffMocker mocker = new DiffMocker(MockerCategory.DATABASE);
        int count = target.saveList(Collections.singletonList(mocker));
        assertEquals(1, count);
    }

    @Test
    @Order(4)
    void query() {
        AbstractMocker mocker = new DatabaseMocker();
        AbstractMocker result = target.query(mocker);
        assertNotNull(result);
    }

    @Test
    @Order(5)
    void testQuery() {
        String sql = "SELECT * FROM MOCKER_INFO WHERE 1 = 1 AND REPLAYID = '' AND CATEGORY = '3' ORDER BY CREATETIME DESC";
        List<Map<String, String>> result = target.query(sql);
        assertTrue(result.size() > 0);
    }

    @Test
    @Order(6)
    void queryList() {
        AbstractMocker mocker = new DatabaseMocker();
        List<AbstractMocker> result = target.queryList(mocker, 10);
        assertTrue(result.size() > 0);
    }

    @Test
    @Order(7)
    void testQueryList() {
        DiffMocker mocker = new DiffMocker(MockerCategory.DATABASE);
        List<DiffMocker> result = target.queryList(mocker);
        assertTrue(result.size() > 0);
    }
}