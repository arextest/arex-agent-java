package io.arex.agent.thirdparty.util.parse.sqlparse.parse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class UpdateSetParse {

    public static void parse(List<UpdateSet> updateSets, ObjectNode sqlObject) {
        if (updateSets != null && !updateSets.isEmpty()) {
            ObjectNode setObj = JacksonHelperUtil.getObjectNode();
            for (UpdateSet updateSet : updateSets) {
                ArrayList<Column> columns = updateSet.getColumns();
                ArrayList<Expression> expressions = updateSet.getExpressions();
                setObj.put(columns.get(0).toString(), expressions.get(0).toString());
            }
            sqlObject.set(DbParseConstants.COLUMNS, setObj);
        }
    }
}
