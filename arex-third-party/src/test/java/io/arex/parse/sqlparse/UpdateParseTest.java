package io.arex.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.management.OperatingSystemMXBean;
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
public class UpdateParseTest {

    private long startTime;
    private long startCpuTime;
    private long startUserTime;
    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static final SqlParseManager sqlParseManager = SqlParseManager.getInstance();

    private static JsonNode parse(String sql) throws JSQLParserException {
        return sqlParseManager.parse(sql);
    }


//    @BeforeEach
//    public void setUp() {
//        // 获取开始时间
//        startTime = System.nanoTime();
//
//        startCpuTime = threadBean.getCurrentThreadCpuTime();
//        startUserTime = threadBean.getCurrentThreadUserTime();
//    }
//
//    @AfterEach
//    public void tearDown() {
//        //获取耗时以及cpu使用率
//        long endTime = System.nanoTime();
//
//        long duration = endTime - startTime;
//        System.out.println("cost time: " + duration / 1_000_000.0 + " ms");
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

    @Test
    public void testUpdate1() throws JSQLParserException {
        String sql = "UPDATE Websites \n" +
                "SET alexa='5000', country='USA' \n" +
                "WHERE name='菜鸟教程';";

        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.UPDATE, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("Websites", ParseUtil.parseUpdateTable(parse));

        System.out.println("parse: " + parse);
    }

    @Test
    public void testUpdate2() throws JSQLParserException {
        String sql = "update test set column = 1 order by id desc limit 2\n";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.UPDATE, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("test", ParseUtil.parseUpdateTable(parse));
    }

    @Test
    public void testUpdate3() throws JSQLParserException {
        String sql = "UPDATE tablea a\n" +
                "JOIN tableb b\n" +
                "SET a.val = b.val\n" +
                "WHERE b.id = a.id";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.UPDATE, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("tablea", ParseUtil.parseUpdateTable(parse));
    }

    @Test
    public void testUpdate4() throws JSQLParserException {

        startCpuTime = threadBean.getCurrentThreadCpuTime();
        startUserTime = threadBean.getCurrentThreadUserTime();
        // 获取开始时间
        startTime = System.currentTimeMillis();


        String sql = "UPDATE `hotelpicture` SET `hotelid`=1026268, `title`='外观', `smallpicurl`='', "
                + "`largepicurl`='', `description`='外观', `sort`=0, `newpicurl`='/0206f120009irgqljCA50.jpg', "
                + "`pictype`=100, `position`='H', `typeid`=0, `sharpness`=null WHERE `id`=492752329";
        JsonNode parse = parse(sql);
        System.out.println("parse: " + parse);
//        assertEquals(DbParseConstants.UPDATE, parse.get(DbParseConstants.ACTION).asText());
//        System.out.println("parse: " + parse);

//        Map<String, String> tableAndAction = SqlParseManager.getInstance().parseTableAndAction(sql);
//        assertEquals("`hotelpicture`", tableAndAction.get(DbParseConstants.TABLE));
//        assertEquals(DbParseConstants.UPDATE, tableAndAction.get(DbParseConstants.ACTION));

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
}
