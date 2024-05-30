package io.arex.agent.thirdparty.util.parse.sqlparse.action;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.thirdparty.util.JacksonHelperUtil;
import io.arex.agent.thirdparty.util.parse.sqlparse.constants.DbParseConstants;

/**
 * @author niyan
 * @date 2024/4/8
 * @since 1.0.0
 */
public abstract class AbstractParse<T> {

    protected final ObjectNode sqlObjectNode;

    public AbstractParse() {
        sqlObjectNode = JacksonHelperUtil.getObjectNode();
    }

    public AbstractParse(String action) {
        this();
        sqlObjectNode.put(DbParseConstants.ACTION, action);
    }

    public abstract ObjectNode parse(T parseObj);
}
