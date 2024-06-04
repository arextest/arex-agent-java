package io.arex.inst.runtime.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseUtilsTest {

    @ParameterizedTest
    @CsvSource(value ={
            "database1, @, select * from table1, @",
            "database1, query, ;, ''",
            "database1, query, mock, ''",
            "database1, query, select * from table1, database1@table1@Select@query"
    }, nullValues={"null"})
    void regenerateOperationName(String dbName, String operationName, String sqlText, String expect) {
        assertEquals(expect, DatabaseUtils.regenerateOperationName(dbName, operationName, sqlText));
    }
}