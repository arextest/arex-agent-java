package io.arex.agent.thirdparty.util.parse.sqlparse.parse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.adapter.ArexItemsListVisitorAdapter;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;

import java.util.List;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class ColumnParse {

    public static void parse(List<Column> columns, ItemsList itemsList, ObjectNode sqlObject) {
        if (columns != null && !columns.isEmpty()) {
            ArrayNode sqlColumnArr = JacksonHelperUtil.getArrayNode();
            ArrayNode values = JacksonHelperUtil.getArrayNode();
            if (itemsList != null) {
                itemsList.accept(new ArexItemsListVisitorAdapter(values));
                for (int i = 0; i < values.size(); i++) {
                    ObjectNode sqlColumnItem = JacksonHelperUtil.getObjectNode();
                    ArrayNode columnValueArray = (ArrayNode) values.get(i);
                    int columnValueSize = columnValueArray.size();
                    for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                        JsonNode value = new TextNode("?");
                        if (columnIndex < columnValueSize) {
                            value = columnValueArray.get(columnIndex);
                        }
                        sqlColumnItem.set(columns.get(columnIndex).toString(), value);
                    }
                    sqlColumnArr.add(sqlColumnItem);
                }
                sqlObject.set(DbParseConstants.COLUMNS, sqlColumnArr);
            }

        }
    }

    public static void parseSetColumns(List<Column> setColumns, List<Expression> setExpressionList, ObjectNode sqlObject) {
        if (setColumns != null && !setColumns.isEmpty()) {
            ArrayNode sqlColumnArr = JacksonHelperUtil.getArrayNode();
            ObjectNode setColumnObj = JacksonHelperUtil.getObjectNode();
            ArrayNode values = JacksonHelperUtil.getArrayNode();
            for (Expression expression : setExpressionList) {
                values.add(expression.toString());
            }
            for (int i = 0; i < setColumns.size(); i++) {
                Object value = "?";
                if (i < values.size()) {
                    value = values.get(i);
                }
                setColumnObj.putPOJO(setColumns.get(i).toString(), value);
            }
            sqlColumnArr.add(setColumnObj);
            sqlObject.set(DbParseConstants.COLUMNS, sqlColumnArr);
        }
    }
}
