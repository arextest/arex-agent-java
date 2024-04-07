package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import io.arex.agent.thirdparty.util.parse.sqlparse.Parse;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import net.sf.jsqlparser.statement.Statement;


/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public class ActionFactory {
    public static Parse selectParse(Statement statement) {
        if (statement instanceof Select) {
            return new SelectParse();
        } else if (statement instanceof Execute) {
            return new ExecuteParse();
        } else if (statement instanceof Delete) {
            return new DeleteParse();
        } else if (statement instanceof Insert) {
            return new InsertParse();
        } else if (statement instanceof Replace) {
            return new ReplaceParse();
        } else if (statement instanceof Update) {
            return new UpdateParse();
        } else {
            throw new UnsupportedOperationException("not support");
        }
    }
}
