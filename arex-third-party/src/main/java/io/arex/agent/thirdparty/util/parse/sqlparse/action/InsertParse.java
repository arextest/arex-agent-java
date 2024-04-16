package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.ColumnParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.parse.TableParse;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.insert.Insert;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class InsertParse extends AbstractParse<Insert> {

    public InsertParse() {
        super(DbParseConstants.INSERT);
    }
    @Override
    public ObjectNode parse(Insert parseObj) {
        // table parse
        TableParse.parse(parseObj.getTable(), sqlObjectNode);

        // columns parse
        ColumnParse.parse(parseObj.getColumns(), parseObj.getItemsList(), sqlObjectNode);

        // setColumns parse
        ColumnParse.parseSetColumns(parseObj.getSetColumns(), parseObj.getSetExpressionList(), sqlObjectNode);

        return sqlObjectNode;
    }
}
