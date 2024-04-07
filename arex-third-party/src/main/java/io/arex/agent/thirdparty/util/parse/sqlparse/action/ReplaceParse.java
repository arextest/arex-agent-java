package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.Parse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ColumnParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ExpressionParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.TableParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.replace.Replace;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class ReplaceParse implements Parse<Replace> {

    private final ObjectNode replaceObj;
    public ReplaceParse() {
        replaceObj = JacksonHelperUtil.getObjectNode();
        // action parse
        replaceObj.put(DbParseConstants.ACTION, DbParseConstants.REPLACE);
    }
    @Override
    public ObjectNode parse(Replace parseObj) {
        // table parse
        TableParse.parse(parseObj.getTable(), replaceObj);

        // columns parse
        ColumnParse.parse(parseObj.getColumns(), parseObj.getItemsList(), replaceObj);

        // expressions parse
        ExpressionParse.parse(parseObj.getExpressions(), parseObj.getColumns(), replaceObj);

        return replaceObj;
    }
}
