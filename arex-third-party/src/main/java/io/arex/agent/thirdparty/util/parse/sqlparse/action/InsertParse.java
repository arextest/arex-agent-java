package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.Parse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ColumnParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.TableParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.insert.Insert;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class InsertParse implements Parse<Insert> {

    private final ObjectNode insertObj;
    public InsertParse() {
        insertObj = JacksonHelperUtil.getObjectNode();
        // action parse
        insertObj.put(DbParseConstants.ACTION, DbParseConstants.INSERT);
    }
    @Override
    public ObjectNode parse(Insert parseObj) {
        // table parse
        TableParse.parse(parseObj.getTable(), insertObj);

        // columns parse
        ColumnParse.parse(parseObj.getColumns(), parseObj.getItemsList(), insertObj);

        // setColumns parse
        ColumnParse.parseSetColumns(parseObj.getSetColumns(), parseObj.getSetExpressionList(), insertObj);

        return insertObj;
    }
}
