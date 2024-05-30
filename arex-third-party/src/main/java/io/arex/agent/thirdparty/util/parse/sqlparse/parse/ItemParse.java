package io.arex.agent.thirdparty.util.parse.sqlparse.parse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.adapter.ArexFromItemVisitorAdapter;
import io.arex.agent.thirdparty.util.parse.sqlparse.adapter.ArexSelectItemVisitorAdapter;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;

/**
 * @author niyan
 * @date 2024/4/7
 * @since 1.0.0
 */
public class ItemParse {

    public static void parseFromItem(FromItem fromItem, ObjectNode sqlObject) {
        if (fromItem != null) {
            ObjectNode fromObj = JacksonHelperUtil.getObjectNode();
            ArexFromItemVisitorAdapter arexFromItemVisitorAdapter = new ArexFromItemVisitorAdapter(
                    fromObj);
            fromItem.accept(arexFromItemVisitorAdapter);
            sqlObject.set(DbParseConstants.FROM, fromObj);
        }
    }

    public static void parseSelectItem(List<SelectItem> selectItems, ObjectNode sqlObject) {
        if (selectItems != null && !selectItems.isEmpty()) {
            ObjectNode columnsObj = JacksonHelperUtil.getObjectNode();
            ArexSelectItemVisitorAdapter arexSelectItemVisitorAdapter = new ArexSelectItemVisitorAdapter(
                    columnsObj);
            selectItems.forEach(selectItem -> {
                selectItem.accept(arexSelectItemVisitorAdapter);
            });
            sqlObject.set(DbParseConstants.COLUMNS, columnsObj);
        }
    }
}
