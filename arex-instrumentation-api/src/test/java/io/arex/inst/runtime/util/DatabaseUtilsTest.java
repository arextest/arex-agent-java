package io.arex.inst.runtime.util;

import io.arex.inst.runtime.context.ArexContext;
import io.arex.inst.runtime.context.ContextManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DatabaseUtilsTest {

    static String largeSql;

    @BeforeAll
    static void setUp() throws IOException {
        Mockito.mockStatic(ContextManager.class);
        largeSql = new String(Files.readAllBytes(Paths.get("src/test/resources/test_sql.txt")));
    }

    @AfterAll
    static void tearDown() {
        largeSql = null;
        Mockito.clearAllCaches();
    }

    @ParameterizedTest
    @MethodSource("regenerateOperationNameCase")
    void regenerateOperationName(String dbName, String operationName, String sqlText, Runnable mocker, Predicate<String> predicate) {
        mocker.run();
        String result = DatabaseUtils.regenerateOperationName(dbName, operationName, sqlText);
        assertTrue(predicate.test(result));
    }

    static Stream<Arguments> regenerateOperationNameCase() {
        Map<String, Set<String>> excludeMockTemplate = new HashMap<>();
        Set<String> operationSet = new HashSet<>();
        ArexContext context = ArexContext.of("mock");
        Runnable noExcludeMockTemplateMocker = () -> {
            Mockito.when(ContextManager.currentContext()).thenReturn(context);
        };
        Runnable noOperationSetMocker = () -> {
            context.setExcludeMockTemplate(excludeMockTemplate);
        };
        Runnable hasOperationSetMocker = () -> {
            operationSet.add("operation1");
            excludeMockTemplate.put("database1", operationSet);
        };
        Runnable needRegenerateMocker = () -> {
            operationSet.clear();
            operationSet.add("database1@table1@Select@query");
            excludeMockTemplate.put("database1", operationSet);
        };

        Predicate<String> predicate1 = "@"::equals;
        Predicate<String> predicate2 = "database1@table1@Select@query"::equals;

        String normalSql = "select * from table1";

        return Stream.of(
                arguments("database1", "@", normalSql, noExcludeMockTemplateMocker, predicate1),
                arguments("database1", "@", normalSql, noOperationSetMocker, predicate1),
                arguments("database1", "@", normalSql, hasOperationSetMocker, predicate1),
                arguments("database1", "@", largeSql, needRegenerateMocker, predicate1),
                arguments("database1", "query", normalSql, needRegenerateMocker, predicate2),
                arguments("database1", "@", "wrong sql", needRegenerateMocker, predicate1)
        );
    }
}