package io.arex.cli.storage;

import io.arex.foundation.model.AbstractMocker;
import io.arex.foundation.model.DatabaseMocker;
import io.arex.foundation.model.DiffMocker;
import io.arex.foundation.model.MockerCategory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
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
        AbstractMocker mocker = new DatabaseMocker();
        mockers.add(mocker);
        String sql = H2SqlParser.generateInsertSql(mockers, tableName, "");
        assertNotNull(sql);
    }

    @ParameterizedTest
    @MethodSource("generateSelectSqlCase")
    void generateSelectSql(AbstractMocker mocker, int count, Predicate<String> predicate) {
        String sql = H2SqlParser.generateSelectSql(mocker, count);
        assertTrue(predicate.test(sql));
    }

    static Stream<Arguments> generateSelectSqlCase() {
        AbstractMocker mocker1 = new DatabaseMocker();
        mocker1.setCaseId(caseId);
        mocker1.setReplayId(replayId);
        AbstractMocker mocker2 = new DatabaseMocker();
        mocker2.setCaseId(caseId);

        Predicate<String> predicate1 = sql -> sql.contains("LIMIT 1");
        Predicate<String> predicate2 = sql -> sql.contains("REPLAYID = ''");

        return Stream.of(
                arguments(mocker1, 1, predicate1),
                arguments(mocker2, 0, predicate2)
        );
    }

    @Test
    void generateSelectDiffSql() {
        DiffMocker mocker = new DiffMocker(MockerCategory.DATABASE);
        mocker.setCaseId(caseId);
        mocker.setReplayId(replayId);
        String result = H2SqlParser.generateSelectDiffSql(mocker);
        assertNotNull(result);
    }
}