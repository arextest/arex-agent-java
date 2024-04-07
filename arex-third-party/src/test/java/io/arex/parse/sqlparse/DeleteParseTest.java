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
public class DeleteParseTest {

    private static JsonNode parse(String sql) throws JSQLParserException {
        SqlParse sqlParse = new SqlParse();
        return sqlParse.parse(sql);
    }

    @Test
    public void testDelete1() throws JSQLParserException {
        String sql = "DELETE \n" +
                "FROM Exam\n" +
                "WHERE S_date NOT IN \n" +
                "   (\n" +
                "      SELECT \n" +
                "         e2.maxdt\n" +
                "      FROM \n" +
                "         (\n" +
                "            SELECT \n" +
                "               Order_Id, Product_Id, Amt, MAX(S_date) AS maxdt\n" +
                "            FROM Exam\n" +
                "            GROUP BY \n" +
                "               Order_Id, \n" +
                "               Product_Id, \n" +
                "               Amt\n" +
                "         )  AS e2\n" +
                "   );";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.DELETE, result.get(DbParseConstants.ACTION).asText());
        assertEquals("Exam", ParseUtil.parseDeleteTable(result).get(0));
    }

    @Test
    public void testDelete2() throws JSQLParserException {
        String sql = "DELETE \n" +
                "FROM t8\n" +
                "   ORDER BY age DESC\n" +
                "   LIMIT 5;";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.DELETE, result.get(DbParseConstants.ACTION).asText());
        assertEquals("t8", ParseUtil.parseDeleteTable(result).get(0));
    }

    @Test
    public void testDelete3() throws JSQLParserException {
        String sql = "DELETE T1,T2\n" +
                "FROM T1 \n" +
                "   INNER JOIN T2 ON T1.student_id = T2.student.id\n" +
                "WHERE T1.student_id = 2;";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.DELETE, result.get(DbParseConstants.ACTION).asText());
        assertEquals(2, ParseUtil.parseDeleteTable(result).size());
    }
}
