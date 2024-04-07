package io.arex.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.SqlParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class ExecuteParseTest {
    private static JsonNode parse(String sql) throws JSQLParserException {
        SqlParse sqlParse = new SqlParse();
        return sqlParse.parse(sql);
    }

    @Test
    public void testExecute() throws JSQLParserException {
        String sql = "EXEC cp_petowner @ownername='20,30'";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.EXECUTE, result.get("action").asText());
        assertEquals("cp_petowner", result.get(DbParseConstants.EXECUTE_NAME).asText());
    }

    @Test
    public void testExecute1() throws JSQLParserException {
        String sql = "EXEC my_proc 'abc', 123;";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.EXECUTE, result.get("action").asText());
        assertEquals("my_proc", result.get(DbParseConstants.EXECUTE_NAME).asText());
    }
}
