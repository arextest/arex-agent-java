package io.arex.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.SqlParseManager;
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
public class UpdateParseTest {

    private static JsonNode parse(String sql) throws JSQLParserException {
        return SqlParseManager.getInstance().parse(sql);
    }

    @Test
    public void testUpdate1() throws JSQLParserException {
        String sql = "UPDATE Websites \n" +
                "SET alexa='5000', country='USA' \n" +
                "WHERE name='菜鸟教程';";

        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.UPDATE, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("Websites", ParseUtil.parseUpdateTable(parse));
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
        String sql = "UPDATE `hotelpicture` SET `hotelid`=1026268, `title`='外观', `smallpicurl`='', "
                + "`largepicurl`='', `description`='外观', `sort`=0, `newpicurl`='/0206f120009irgqljCA50.jpg', "
                + "`pictype`=100, `position`='H', `typeid`=0, `sharpness`=null WHERE `id`=492752329";
        JsonNode parse = parse(sql);
        assertEquals(DbParseConstants.UPDATE, parse.get(DbParseConstants.ACTION).asText());
        assertEquals("hotelpicture", ParseUtil.parseUpdateTable(parse));
    }
}
