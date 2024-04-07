package io.arex.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.SqlParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.thirdparty.util.parse.sqlparse.util.ParseUtil;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class InsertParseTest {

    private static JsonNode parse(String sql) throws JSQLParserException {
        SqlParse sqlParse = new SqlParse();
        return sqlParse.parse(sql);
    }


    @Test
    public void testInsert() throws JSQLParserException {
        String sql = "insert into stadium(visit_date, people)\n" +
                "values\n" +
                "('2017-01-07' , 199)\n" +
                ",('2017-01-09' , 188)";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.INSERT, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("stadium", ParseUtil.parseInsertTable(parse));
    }

    @Test
    public void testInsert2() throws JSQLParserException {
        String sql = "INSERT INTO category_stage (\n" +
                "   SELECT \n" +
                "      *\n" +
                "   FROM category );";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.INSERT, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("category_stage", ParseUtil.parseInsertTable(parse));
    }

    @Test
    public void testInsert3() throws JSQLParserException {
        String sql = "INSERT INTO MyTable (Text) VALUES ('A'||CHAR(10)||'B')";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.INSERT, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("MyTable", ParseUtil.parseInsertTable(parse));
    }

    @Test
    public void testInsert4() throws JSQLParserException {
        String sql = "INSERT INTO users SET id = 123, name = '姚明', age = 25;";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.INSERT, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("users", ParseUtil.parseInsertTable(parse));
    }
}
