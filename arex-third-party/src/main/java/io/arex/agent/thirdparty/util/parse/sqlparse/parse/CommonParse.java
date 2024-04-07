package io.arex.agent.thirdparty.util.parse.sqlparse.parse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OptimizeFor;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.Wait;

import java.util.List;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class CommonParse {

    public static void parseDistinct(Distinct distinct, ObjectNode sqlObject) {
        // parse distinct
        if (distinct != null) {
            sqlObject.put(DbParseConstants.DISTINCT, distinct.toString());
        }
    }

    public static void parseSkip(Skip skip, ObjectNode sqlObject) {
        // parse skip
        if (skip != null) {
            sqlObject.put(DbParseConstants.SKIP, skip.toString());
        }
    }

    public static void parseTop(Top top, ObjectNode sqlObject) {
        // parse top
        if (top != null) {
            sqlObject.put(DbParseConstants.TOP, top.toString());
        }
    }

    public static void parseFirst(First first, ObjectNode sqlObject) {
        // parse first
        if (first != null) {
            sqlObject.put(DbParseConstants.FIRST, first.toString());
        }
    }

    public static void parseInto(List<Table> intoTables, ObjectNode sqlObject) {
        // parse into
        if (intoTables != null && !intoTables.isEmpty()) {
            sqlObject.put(DbParseConstants.INTO, intoTables.toString());
        }
    }

    public static void parseGroupByElement(GroupByElement groupByElement, ObjectNode sqlObject) {
        // parse group by
        if (groupByElement != null) {
            sqlObject.put(DbParseConstants.GROUP_BY, groupByElement.toString());
        }
    }

    public static void parseLimit(Limit limit, ObjectNode sqlObjectNode) {
        if (limit != null) {
            sqlObjectNode.put(DbParseConstants.LIMIT, limit.toString());
        }
    }

    public static void parseFetch(Fetch fetch, ObjectNode sqlObject) {
        if (fetch != null) {
            sqlObject.put(DbParseConstants.FETCH, fetch.toString());
        }
    }

    public static void parseOptimizeFor(OptimizeFor optimizeFor, ObjectNode sqlObject) {
        if (optimizeFor != null) {
            sqlObject.put(DbParseConstants.OPTIMIZE_FOR, optimizeFor.toString());
        }
    }

    public static void parseOffset(Offset offset, ObjectNode sqlObject) {
        if (offset != null) {
            sqlObject.put(DbParseConstants.OFFSET, offset.toString());
        }
    }

    public static void parseForUpdate(boolean forUpdate, ObjectNode sqlObject) {
        if (forUpdate) {
            sqlObject.put(DbParseConstants.FOR_UPDATE, true);
        }
    }

    public static void parseNoWait(boolean noWait, ObjectNode sqlObject) {
        if (noWait) {
            sqlObject.put(DbParseConstants.NO_WAIT, true);
        }
    }

    public static void parseWait(Wait wait, ObjectNode sqlObject) {
        if (wait != null) {
            sqlObject.put(DbParseConstants.WAIT, wait.toString());
        }
    }

    public static void parseName(String name, ObjectNode sqlObject) {
        // parse name
        if (name != null && !name.isEmpty()) {
            sqlObject.put(DbParseConstants.EXECUTE_NAME, name);
        }
    }
}
