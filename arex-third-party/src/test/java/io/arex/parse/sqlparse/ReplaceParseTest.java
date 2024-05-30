package io.arex.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.SqlParseManager;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.thirdparty.util.parse.sqlparse.util.ParseUtil;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class ReplaceParseTest {
    private static JsonNode parse(String sql) throws JSQLParserException {
        return SqlParseManager.getInstance().parse(sql);
    }

    @Test
    public void testReplace() throws JSQLParserException {
        String sql =
                "REPLACE INTO orderTable(OrderId, InfoId, DataChange_LastTime, userdata_location) " +
                        "VALUES (36768383786, 36768317034, '2023-05-14 18:00:34.556', '')," +
                        "(36768317034, 36768317034, '2023-05-14 18:00:34.556', '')";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.REPLACE, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("orderTable", ParseUtil.parseReplayTable(parse));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("orderTable", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.REPLACE, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testReplace2() throws JSQLParserException {
        String sql = "replace into tb1(name, title, mood) select  rname, rtitle, rmood from tb2";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.REPLACE, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("tb1", ParseUtil.parseReplayTable(parse));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("tb1", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.REPLACE, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testReplace3() throws JSQLParserException {
        String sql = "replace into tb1 set name = 'name', title = 'title',mood = 1";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.REPLACE, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("tb1", ParseUtil.parseReplayTable(parse));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("tb1", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.REPLACE, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testReplace4() throws JSQLParserException {
        String sql =
                "REPLACE INTO orderTable(OrderId, InfoId, DataChange_LastTime, userdata_location) " +
                        "VALUES (?, ?, ?, ?)";

        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.REPLACE, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("orderTable", ParseUtil.parseReplayTable(parse));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("orderTable", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.REPLACE, tableAndAction.get(DbParseConstants.ACTION));
    }
}
