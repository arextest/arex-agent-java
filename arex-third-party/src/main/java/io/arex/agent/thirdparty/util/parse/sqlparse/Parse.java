package io.arex.agent.thirdparty.util.parse.sqlparse;


import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author niyan
 * @date 2024/4/3
 * @since 1.0.0
 */
public interface Parse<T> {
    ObjectNode parse(T parseObj);
}
