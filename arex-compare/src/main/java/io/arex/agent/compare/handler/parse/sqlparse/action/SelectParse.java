package io.arex.agent.compare.handler.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.compare.handler.parse.sqlparse.Parse;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.handler.parse.sqlparse.select.ArexSelectVisitorAdapter;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

/**
 * the example of parsed select sql: { "action": "SELECT", "columns":
 * { "a.Salary": "" }, "from": { "table": { "action": "SELECT", "columns": { "*": "", "dense_rank()
 * over(partition by departmentid order by Salary desc) as rnk": "" }, "from": { "table": [
 * "Employee" ] } }, "alias": "a" }, "join": [ { "type": "LEFT join", "table": "department b", "on":
 * { "a.departmentid = b.Id AND a.aa = b.aa": "" } } ], "where": { "andor": [ "and", "and" ],
 * "columns": { "a.rnk <= 3": "", "a.per_id in (select per_id from colle_subject)": "" } } }
 */
public class SelectParse implements Parse<Select> {

    @Override
    public ObjectNode parse(Select parseObj) {
        ObjectNode sqlObject = JacksonHelperUtil.getObjectNode();
        sqlObject.put(DbParseConstants.ACTION, DbParseConstants.SELECT);
        SelectBody selectBody = parseObj.getSelectBody();
        ArexSelectVisitorAdapter arexSelectVisitorAdapter = new ArexSelectVisitorAdapter(sqlObject);
        selectBody.accept(arexSelectVisitorAdapter);
        return sqlObject;
    }
}
