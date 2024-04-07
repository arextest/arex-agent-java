package io.arex.agent.thirdparty.util.parse.sqlparse.parse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.schema.Table;

import java.util.List;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class TableParse {

    public static void parse(Table table, ObjectNode sqlObject) {
        // parse table
        if (table != null) {
            sqlObject.put(DbParseConstants.TABLE, table.getFullyQualifiedName());
        }
    }

    public static void parseDelTable(List<Table> tables, Table table, ObjectNode sqlObject) {
        // parse tables
        if (tables != null && !tables.isEmpty()) {
            ObjectNode delTableObj = JacksonHelperUtil.getObjectNode();
            tables.forEach(item -> {
                delTableObj.put(item.getFullyQualifiedName(), DbParseConstants.EMPTY);
            });
            sqlObject.set(DbParseConstants.DEL_TABLES, delTableObj);
        } else {
            sqlObject.put(DbParseConstants.DEL_TABLES, table.getFullyQualifiedName());
        }
    }

    public static void parseForUpdateTable(Table forUpdateTable, ObjectNode sqlObject) {
        // parse for update table
        if (forUpdateTable != null) {
            sqlObject.put(DbParseConstants.FOR_UPDATE_TABLE, forUpdateTable.toString());
        }

    }
}
