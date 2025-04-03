package io.arex.agent.compare.handler.parse.sqlparse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.arex.agent.bootstrap.internal.Pair;
import io.arex.agent.compare.handler.parse.sqlparse.action.ActionFactory;
import io.arex.agent.compare.handler.parse.sqlparse.constants.DbParseConstants;
import io.arex.agent.compare.utils.JacksonHelperUtil;
import io.arex.agent.compare.utils.LogUtil;
import io.arex.agent.compare.utils.NameConvertUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.ArrayList;
import java.util.List;

public class SqlParse {
    // if return null, indicate the sql parsed fail.
    public ParsedResult sqlParse(ObjectNode jsonObj, boolean nameToLower) {
        JsonNode databaseBody = jsonObj.get(DbParseConstants.BODY);
        if (databaseBody == null) {
            return new ParsedResult(null, false);
        }

        boolean successParse = true;
        ArrayNode parsedSql = JacksonHelperUtil.getArrayNode();
        List<Boolean> isSelect = new ArrayList<>();
        try {
            if (databaseBody instanceof TextNode) {
                Pair<JsonNode, Boolean> tempMutablePair = sqlParse(databaseBody.asText());
                parsedSql.add(tempMutablePair.getFirst());
                isSelect.add(tempMutablePair.getSecond());
            } else if (databaseBody instanceof ArrayNode) {
                ArrayNode databaseBodyArray = (ArrayNode) databaseBody;
                for (int i = 0; i < databaseBodyArray.size(); i++) {
                    Pair<JsonNode, Boolean> tempMutablePair = sqlParse(databaseBodyArray.get(i).asText());
                    parsedSql.add(tempMutablePair.getFirst());
                    isSelect.add(tempMutablePair.getSecond());
                }
            } else {
                successParse = false;
            }
        } catch (Throwable throwable) {
            LogUtil.warn("arex sqlParse error", throwable);
            successParse = false;
        }

        ParsedResult result = new ParsedResult();
        if (!successParse) {
            this.fillOriginalSql(jsonObj, databaseBody);
            result.setSuccess(false);
        } else {
            if (nameToLower) {
                NameConvertUtil.nameConvert(parsedSql);
            }
            jsonObj.set(DbParseConstants.PARSED_SQL, parsedSql);
            result.setSuccess(true);
            result.setIsSelect(isSelect);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Pair<JsonNode, Boolean> sqlParse(String sql) throws JSQLParserException {
        if (sql == null || sql.length() > DbParseConstants.SQL_LENGTH_LIMIT) {
            throw new RuntimeException("sql is null or too long");
        }
        Statement statement = CCJSqlParserUtil.parse(sql);
        Parse parse = ActionFactory.selectParse(statement);
        return Pair.of(parse.parse(statement), statement instanceof Select);
    }

    private void fillOriginalSql(ObjectNode objectNode, JsonNode databaseBody) {
        ObjectNode backUpObj = JacksonHelperUtil.getObjectNode();
        backUpObj.set(DbParseConstants.ORIGINAL_SQL, databaseBody);
        ArrayNode parsedSql = JacksonHelperUtil.getArrayNode();
        parsedSql.add(backUpObj);
        objectNode.set(DbParseConstants.PARSED_SQL, parsedSql);
    }

    private static class ParsedResult {

        private List<Boolean> isSelect;
        private boolean success;

        public ParsedResult() {

        }

        public ParsedResult(boolean success) {
            this.success = success;
        }

        public ParsedResult(List<Boolean> isSelect) {
            this.isSelect = isSelect;
        }

        public ParsedResult(List<Boolean> isSelect, boolean success) {
            this.isSelect = isSelect;
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public List<Boolean> getIsSelect() {
            return isSelect;
        }

        public void setIsSelect(List<Boolean> isSelect) {
            this.isSelect = isSelect;
        }
    }
}
