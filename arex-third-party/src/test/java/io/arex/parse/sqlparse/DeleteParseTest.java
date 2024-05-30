package io.arex.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.SqlParseManager;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.thirdparty.util.parse.sqlparse.util.ParseUtil;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class DeleteParseTest {

    private long startTime;
    private long startCpuTime;
    private long startUserTime;
    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static final SqlParseManager sqlParseManager = SqlParseManager.getInstance();

    private static JsonNode parse(String sql) throws JSQLParserException {
        return sqlParseManager.parse(sql);
    }

    @BeforeEach
    public void setUp() {
        // 获取开始时间
        startTime = System.currentTimeMillis();

        startCpuTime = threadBean.getCurrentThreadCpuTime();
        startUserTime = threadBean.getCurrentThreadUserTime();
    }

    @AfterEach
    public void tearDown() {
        //获取耗时以及cpu使用率
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.println("cost time: " + duration + " ms");

        long endCpuTime = threadBean.getCurrentThreadCpuTime();
        long endUserTime = threadBean.getCurrentThreadUserTime();

        double cpuTime = (endCpuTime - startCpuTime) / 1_000_000.0;
        double userTime = (endUserTime - startUserTime) / 1_000_000.0;

        System.out.println("CPU time: " + cpuTime + " ms");
        System.out.println("User time: " + userTime + " ms");
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
                "            FROM TEST\n" +
                "            GROUP BY \n" +
                "               Order_Id, \n" +
                "               Product_Id, \n" +
                "               Amt\n" +
                "         )  AS e2\n" +
                "   );";
//        JsonNode result = parse(sql);
//        assertEquals(DbParseConstants.DELETE, result.get(DbParseConstants.ACTION).asText());
//        assertEquals("Exam", ParseUtil.parseDeleteTable(result).get(0));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
//        assertEquals("Exam,TEST", tableAndAction.get(DbParseConstants.TABLE));
//        assertEquals(DbParseConstants.DELETE, tableAndAction.get(DbParseConstants.ACTION));
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

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("t8", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.DELETE, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testDelete3() throws JSQLParserException {
        String sql = "DELETE T1,T2\n" +
                "FROM T1 \n" +
                "   INNER JOIN T2 ON T1.student_id = T2.student.id\n" +
                "WHERE T1.student_id = 2;";
//        JsonNode result = parse(sql);
//        System.out.println(result);
//        assertEquals(DbParseConstants.DELETE, result.get(DbParseConstants.ACTION).asText());
//        assertEquals(2, ParseUtil.parseDeleteTable(result).size());

        Map<String, String> tableAndAction = sqlParseManager.parseTableAndAction(sql);
//        assertEquals("T1,T2", tableAndAction.get(DbParseConstants.TABLE));
//        assertEquals(DbParseConstants.DELETE, tableAndAction.get(DbParseConstants.ACTION));
    }
}
