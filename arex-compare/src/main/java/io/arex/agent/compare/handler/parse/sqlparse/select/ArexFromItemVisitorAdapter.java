package io.arex.agent.compare.handler.parse.sqlparse.select;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesisFromItem;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.ValuesList;

public class ArexFromItemVisitorAdapter implements FromItemVisitor {

    private Object sqlObj;

    public ArexFromItemVisitorAdapter(ObjectNode object) {
        sqlObj = object;
    }


    @Override
    public void visit(Table table) {
        ObjectNode sqlJSONObj = (ObjectNode) sqlObj;

        // partItems parse
        sqlJSONObj.put(DbParseConstants.TABLE, table.getFullyQualifiedName());

        // alias parse
        Alias alias = table.getAlias();
        if (alias != null) {
            sqlJSONObj.put(DbParseConstants.ALIAS, alias.toString());
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        ObjectNode sqlJSONObj = (ObjectNode) sqlObj;

        // SelectBody parse
        SelectBody selectBody = subSelect.getSelectBody();
        if (selectBody != null) {
            ObjectNode tempSelectBodyObj = JacksonHelperUtil.getObjectNode();
            ArexSelectVisitorAdapter arexSelectVisitorAdapter = new ArexSelectVisitorAdapter(
                    tempSelectBodyObj);
            selectBody.accept(arexSelectVisitorAdapter);
            sqlJSONObj.put(DbParseConstants.TABLE, tempSelectBodyObj);
        }

        // alias parse
        Alias alias = subSelect.getAlias();
        if (alias != null) {
            sqlJSONObj.put(DbParseConstants.ALIAS, alias.toString());
        }
    }

    @Override
    public void visit(SubJoin subJoin) {
        ObjectNode sqlJSONObj = (ObjectNode) sqlObj;
        sqlJSONObj.put(DbParseConstants.TABLE, subJoin.toString());
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        ObjectNode sqlJSONObj = (ObjectNode) sqlObj;
        sqlJSONObj.put(DbParseConstants.TABLE, lateralSubSelect.toString());
    }

    @Override
    public void visit(ValuesList valuesList) {
        ObjectNode sqlJSONObj = (ObjectNode) sqlObj;
        sqlJSONObj.put(DbParseConstants.TABLE, valuesList.toString());
    }

    @Override
    public void visit(TableFunction tableFunction) {
        ObjectNode sqlJSONObj = (ObjectNode) sqlObj;
        sqlJSONObj.put(DbParseConstants.TABLE, tableFunction.toString());
    }

    @Override
    public void visit(ParenthesisFromItem parenthesisFromItem) {
        ObjectNode sqlJSONObj = (ObjectNode) sqlObj;
        sqlJSONObj.put(DbParseConstants.TABLE, parenthesisFromItem.toString());
    }
}
