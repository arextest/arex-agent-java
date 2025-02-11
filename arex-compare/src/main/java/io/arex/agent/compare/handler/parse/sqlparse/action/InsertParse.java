package io.arex.agent.compare.handler.parse.sqlparse.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.arex.agent.compare.handler.parse.sqlparse.Parse;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.handler.parse.sqlparse.select.ArexItemsListVisitorAdapter;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

import java.util.List;

public class InsertParse implements Parse<Insert> {

    @Override
    public ObjectNode parse(Insert parseObj) {
        ObjectNode sqlObject = JacksonHelperUtil.getObjectNode();
        sqlObject.put(DbParseConstants.ACTION, DbParseConstants.INSERT);

        // table parse
        Table table = parseObj.getTable();
        if (table != null) {
            sqlObject.put(DbParseConstants.TABLE, table.getFullyQualifiedName());
        }

        // columns parse
        List<Column> columns = parseObj.getColumns();
        if (columns != null && !columns.isEmpty()) {
            ArrayNode sqlColumnArr = JacksonHelperUtil.getArrayNode();
            ArrayNode values = JacksonHelperUtil.getArrayNode();
            ItemsList itemsList = parseObj.getItemsList();
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

        // setColumns parse
        List<Column> setColumns = parseObj.getSetColumns();
        if (setColumns != null && !setColumns.isEmpty()) {
            ArrayNode sqlColumnArr = JacksonHelperUtil.getArrayNode();
            ObjectNode setColumnObj = JacksonHelperUtil.getObjectNode();
            ArrayNode values = JacksonHelperUtil.getArrayNode();
            List<Expression> setExpressionList = parseObj.getSetExpressionList();
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

        return sqlObject;
    }
}
