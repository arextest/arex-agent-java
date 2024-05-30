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
 * @date 2024/4/3
 * @since 1.0.0
 */
public class SelectParseTest {

    private long startTime;
    private long startCpuTime;
    private long startUserTime;
    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

//    @BeforeEach
//    public void setUp() {
//        startCpuTime = threadBean.getCurrentThreadCpuTime();
//        startUserTime = threadBean.getCurrentThreadUserTime();
//        // 获取开始时间
//        startTime = System.currentTimeMillis();
//    }

//    @AfterEach
//    public void tearDown() {
//        //获取耗时以及cpu使用率
//        long endTime = System.currentTimeMillis();
//
//        long duration = endTime - startTime;
//        System.out.println("cost time: " + duration + " ms");
//
//        long endCpuTime = threadBean.getCurrentThreadCpuTime();
//        long endUserTime = threadBean.getCurrentThreadUserTime();
//
//        double cpuTime = (endCpuTime - startCpuTime) / 1_000_000.0;
//        double userTime = (endUserTime - startUserTime) / 1_000_000.0;
//
//        System.out.println("CPU time: " + cpuTime + " ms");
//        System.out.println("User time: " + userTime + " ms");
//    }


    private static JsonNode parse(String sql) throws JSQLParserException {
        return SqlParseManager.getInstance().parse(sql);
    }

    @Test
    public void testSimpleSelect() throws JSQLParserException {
        String select = "select * from test";
        JsonNode result = parse(select);
        assertEquals(DbParseConstants.SELECT, result.get(DbParseConstants.ACTION).asText());
        assertEquals("test", ParseUtil.parseSelectTableName(result));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(select);
        assertEquals("test", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.SELECT, tableAndAction.get(DbParseConstants.ACTION));
    }


    @Test
    public void testSelect() throws JSQLParserException {
        String sql = "select b.Name as Department,a.Name as Employee,a.Salary\n" +
                "from (select *,dense_rank() over(partition by departmentid order by Salary desc) as rnk from Employee) a \n"
                +
                "left join department b \n" +
                "on a.departmentid = b.Id and a.aa = b.aa and a.cc = b.cc\n" +
                "where a.rnk <= 3 and a.per_id in (select per_id from colle_subject);\n" +
                "\n";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.SELECT, result.get(DbParseConstants.ACTION).asText());
        assertEquals("Employee", ParseUtil.parseSelectTableName(result));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals(DbParseConstants.SELECT, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testSelect1() throws JSQLParserException {
        String sql = "SELECT * FROM students WHERE scores >> 4 >= 6;";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.SELECT, result.get(DbParseConstants.ACTION).asText());
        assertEquals("students", ParseUtil.parseSelectTableName(result));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("students", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.SELECT, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testSelect2() throws JSQLParserException {
        String sql = "SELECT \n" +
                "   \n" +
                "      CASE \n" +
                "         WHEN (\n" +
                "            CASE \n" +
                "               WHEN (\n" +
                "                  CASE \n" +
                "                     WHEN (\n" +
                "                        CASE \n" +
                "                           WHEN (1) THEN 0\n" +
                "                        END) THEN 0\n" +
                "                  END) THEN 0\n" +
                "            END) THEN 0\n" +
                "      END\n" +
                "FROM a;";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.SELECT, result.get(DbParseConstants.ACTION).asText());
        assertEquals("a", ParseUtil.parseSelectTableName(result));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("a", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.SELECT, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testSelectOrderBy() throws JSQLParserException {
        String sql = "select first 5 Node from alerts.status order by Node asc;";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.SELECT, result.get(DbParseConstants.ACTION).asText());
        assertEquals("alerts.status", ParseUtil.parseSelectTableName(result));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("alerts.status", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.SELECT, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testSelectUnion() throws JSQLParserException {
        String sql = "select st_id from students union select st_id from student_skill;";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.SELECT, result.get(DbParseConstants.ACTION).asText());
    }

    @Test
    public void testSelectWhere() throws JSQLParserException {
        String sql = "SELECT * FROM students WHERE score = 18; select * from student where score = 20;";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.SELECT, result.get(DbParseConstants.ACTION).asText());
        assertEquals("students", ParseUtil.parseSelectTableName(result));
    }

    @Test
    public void testSelectLeftJoin() throws JSQLParserException {

        startCpuTime = threadBean.getCurrentThreadCpuTime();
        startUserTime = threadBean.getCurrentThreadUserTime();
        // 获取开始时间
        startTime = System.currentTimeMillis();

        String sql = "select s.Name,C.Cname from student_course as sc " +
                "left join student as s on s.Sno=sc.Sno" +
                "left join course as c on c.Cno=sc.Cno on s.Sno=sc.Sno;select * from student_course";
//        String sql = "SELECT * FROM [AccountCheck] WITH (NOLOCK) WHERE [AccCheckID]=? AND [ss]=?";

        JsonNode result = parse(sql);
        System.out.println("parse: " + result);

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


        //        assertEquals("student_course,student,course", tableAndAction.get(DbParseConstants.TABLE));
//        assertEquals(DbParseConstants.SELECT, tableAndAction.get(DbParseConstants.ACTION));

        //        JsonNode result = parse(sql);
//        assertEquals(DbParseConstants.SELECT, result.get(DbParseConstants.ACTION).asText());
//        assertEquals("student_course", ParseUtil.parseSelectTableName(result));
//        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
    }

    @Test
    public void testSelect5() throws JSQLParserException {
        String sql = "SELECT \n" +
                "   \n" +
                "      y1 年, \n" +
                "      m1 月, \n" +
                "      c1 本月销售额, \n" +
                "      c2 上月销售额, \n" +
                "      \n" +
                "         CASE \n" +
                "            WHEN c2 IS NULL OR c2 = 0 THEN '无穷大'\n" +
                "            ELSE CAST(CAST((isnull(c1, 0) - isnull(c2, 0)) * 100 / isnull(c2, 0) AS decimal(10, 2)) AS varchar(50)) + '%'\n"
                +
                "         END AS 环比增长, \n" +
                "      c3 去年本月销售额, \n" +
                "      \n" +
                "         CASE \n" +
                "            WHEN c3 IS NULL OR c3 = 0 THEN '无穷大'\n" +
                "            ELSE CAST(CAST((isnull(c1, 0) - isnull(c3, 0)) * 100 / isnull(c3, 0) AS decimal(10, 2)) AS varchar(50)) + '%'\n"
                +
                "         END AS 同比增长\n" +
                "FROM \n" +
                "   (\n" +
                "      SELECT \n" +
                "         \n" +
                "            y1, \n" +
                "            m1, \n" +
                "            c1, \n" +
                "            c2, \n" +
                "            c3\n" +
                "      FROM \n" +
                "         (\n" +
                "            SELECT \n" +
                "               y1, m1, c1, c2\n" +
                "            FROM \n" +
                "               (\n" +
                "                  SELECT \n" +
                "                     y1, m1, sum(Amt) AS c1\n" +
                "                  FROM \n" +
                "                     (\n" +
                "                        SELECT \n" +
                "                           datepart(year, CONVERT(DateTime, s_date)) AS y1, datepart(month, CONVERT(DateTime, s_date)) AS m1, Amt\n"
                +
                "                        FROM orders\n" +
                "                     )  AS T1\n" +
                "                  GROUP BY T1.y1, T1.m1\n" +
                "               )  o2 \n" +
                "               LEFT JOIN \n" +
                "               (\n" +
                "                  SELECT \n" +
                "                     y2, m2, sum(Amt) AS c2\n" +
                "                  FROM \n" +
                "                     (\n" +
                "                        SELECT \n" +
                "                           datepart(year, CONVERT(DateTime, s_date)) AS y2, datepart(month, CONVERT(DateTime, s_date)) AS m2, Amt\n"
                +
                "                        FROM orders\n" +
                "                     )  AS T1\n" +
                "                  GROUP BY T1.y2, T1.m2\n" +
                "               )  o3 ON o2.y1 = o3.y2 AND o2.m1 = o3.m2 - 1\n" +
                "         )  AS o4 \n" +
                "         LEFT JOIN \n" +
                "         (\n" +
                "            SELECT \n" +
                "               y3, m3, sum(Amt) AS c3\n" +
                "            FROM \n" +
                "               (\n" +
                "                  SELECT \n" +
                "                     datepart(year, CONVERT(DateTime, s_date)) AS y3, datepart(month, CONVERT(DateTime, s_date)) AS m3, Amt\n"
                +
                "                  FROM orders\n" +
                "               )  AS T1\n" +
                "            GROUP BY T1.y3, T1.m3\n" +
                "         )  AS o5 ON o4.y1 = o5.y3 - 1 AND o4.m1 = o5.m3\n" +
                "   )  AS o6;";
        JsonNode result = parse(sql);
        assertEquals(DbParseConstants.SELECT, result.get(DbParseConstants.ACTION).asText());
        assertEquals("orders", ParseUtil.parseSelectTableName(result));
        System.out.println("parse: " + result);

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("orders", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.SELECT, tableAndAction.get(DbParseConstants.ACTION));

    }
}
