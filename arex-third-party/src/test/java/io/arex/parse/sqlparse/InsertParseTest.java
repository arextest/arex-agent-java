package io.arex.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.SqlParseManager;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.thirdparty.util.parse.sqlparse.util.ParseUtil;
import net.sf.jsqlparser.JSQLParserException;
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
public class InsertParseTest {

    private long startTime;
    private long startCpuTime;
    private long startUserTime;
    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static final SqlParseManager sqlParseManager = SqlParseManager.getInstance();

    private static JsonNode parse(String sql) throws JSQLParserException {
        return sqlParseManager.parse(sql);
    }


    @Test
    public void testInsert() throws JSQLParserException {

        startCpuTime = threadBean.getCurrentThreadCpuTime();
        startUserTime = threadBean.getCurrentThreadUserTime();
        // 获取开始时间
        startTime = System.currentTimeMillis();

        String sql = "insert into stadium(visit_date, people)\n" +
                "values\n" +
                "('2017-01-07' , 199)\n" +
                ",('2017-01-09' , 188)";
        JsonNode parse = parse(sql);
        System.out.println(parse);
//        assertEquals(DbParseConstants.INSERT, parse.get(DbParseConstants.ACTION).asText());
//        assertEquals("stadium", ParseUtil.parseInsertTable(parse));
//
//        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
//        assertEquals("stadium", tableAndAction.get(DbParseConstants.TABLE));
//        assertEquals(DbParseConstants.INSERT, tableAndAction.get(DbParseConstants.ACTION));


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
    public void testInsert2() throws JSQLParserException {
        String sql = "INSERT INTO category_stage (\n" +
                "   SELECT \n" +
                "      *\n" +
                "   FROM category );";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.INSERT, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("category_stage", ParseUtil.parseInsertTable(parse));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("category_stage", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.INSERT, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testInsert3() throws JSQLParserException {
        String sql = "INSERT INTO MyTable (Text) VALUES ('A'||CHAR(10)||'B')";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.INSERT, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("MyTable", ParseUtil.parseInsertTable(parse));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("MyTable", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.INSERT, tableAndAction.get(DbParseConstants.ACTION));
    }

    @Test
    public void testInsert4() throws JSQLParserException {
        String sql = "INSERT INTO users SET id = 123, name = '姚明', age = 25;";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.INSERT, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("users", ParseUtil.parseInsertTable(parse));

        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
        assertEquals("users", tableAndAction.get(DbParseConstants.TABLE));
        assertEquals(DbParseConstants.INSERT, tableAndAction.get(DbParseConstants.ACTION));
    }
}
