package io.arex.agent.compare.handler.parse.sqlparse.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;

import io.arex.agent.compare.handler.parse.sqlparse.Parse;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.handler.parse.sqlparse.select.ArexItemsListVisitorAdapter;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.replace.Replace;

public class ReplaceParse implements Parse<Replace> {

    @Override
    public ObjectNode parse(Replace parseObj) {
        ObjectNode sqlObject = JacksonHelperUtil.getObjectNode();
        sqlObject.put(DbParseConstants.ACTION, DbParseConstants.REPLACE);

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

        // expressions parse
        List<Expression> expressions = parseObj.getExpressions();
        if (expressions != null && !expressions.isEmpty()) {
            ArrayNode sqlColumnArr = JacksonHelperUtil.getArrayNode();
            ObjectNode setColumnObj = JacksonHelperUtil.getObjectNode();
            ArrayNode values = JacksonHelperUtil.getArrayNode();
            for (Expression expression : expressions) {
                values.add(expression.toString());
            }
            for (int i = 0; i < columns.size(); i++) {
                Object value = "?";
                if (i < values.size()) {
                    value = values.get(i);
                }
                setColumnObj.putPOJO(columns.get(i).toString(), value);
            }
            sqlColumnArr.add(setColumnObj);
            sqlObject.set(DbParseConstants.COLUMNS, sqlColumnArr);
        }

        return sqlObject;
    }
}