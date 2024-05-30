package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.parse.sqlparse.adapter.ArexSelectVisitorAdapter;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class SelectParse extends AbstractParse<Select> {


    public SelectParse() {
        super(DbParseConstants.SELECT);
    }

    @Override
    public ObjectNode parse(Select parseObj) {
        // selectBody parse
        SelectBody selectBody = parseObj.getSelectBody();
        ArexSelectVisitorAdapter arexSelectVisitorAdapter = new ArexSelectVisitorAdapter(sqlObjectNode);
        selectBody.accept(arexSelectVisitorAdapter);
        return sqlObjectNode;
    }
}
