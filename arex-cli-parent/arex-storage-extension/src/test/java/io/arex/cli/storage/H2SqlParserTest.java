package io.arex.cli.storage;

import io.arex.agent.bootstrap.model.MockCategoryType;
import io.arex.agent.bootstrap.model.Mocker;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.services.MockService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class H2SqlParserTest {
    static String caseId = "100019749-0a20b301";
    static String replayId = "100019749-501394";

    @Test
    @Order(1)
    void parseSchema() {
        Map<String, String> schemaMap = H2SqlParser.parseSchema();
        assertTrue(schemaMap.size() > 1);
    }

    @Test
    @Order(2)
    void generateInsertSql() {
        String tableName = "MOCKER_INFO";
        List<Object> mockers = new ArrayList<>();
        Mocker mocker = MockService.createDatabase(tableName);
        mockers.add(mocker);
        String sql = H2SqlParser.generateInsertSql(mockers, tableName, "");
        assertNotNull(sql);
    }

    @ParameterizedTest
    @MethodSource("generateSelectSqlCase")
    void generateSelectSql(Mocker mocker, int count, Predicate<String> predicate) {
        String sql = H2SqlParser.generateSelectSql(mocker, count);
        assertTrue(predicate.test(sql));
    }

    static Stream<Arguments> generateSelectSqlCase() {
        Mocker mocker1 = MockService.createDatabase("test1");
        mocker1.setRecordId(caseId);
        mocker1.setReplayId(replayId);
        Mocker mocker2 = MockService.createDatabase("test1");
        mocker2.setRecordId(caseId);

        Predicate<String> predicate1 = sql -> sql.contains("LIMIT 1");
        Predicate<String> predicate2 = sql -> sql.contains("REPLAYID = ''");

        return Stream.of(
                arguments(mocker1, 1, predicate1),
                arguments(mocker2, 0, predicate2)
        );
    }

    @Test
    void generateSelectDiffSql() {
        DiffMocker mocker = new DiffMocker(MockCategoryType.DATABASE);
        mocker.setRecordId(caseId);
        mocker.setReplayId(replayId);
        String result = H2SqlParser.generateSelectDiffSql(mocker);
        assertNotNull(result);
    }
}