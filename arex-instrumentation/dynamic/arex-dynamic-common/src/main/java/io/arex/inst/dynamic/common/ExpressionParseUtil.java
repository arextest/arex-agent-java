package io.arex.inst.dynamic.common;

import io.arex.agent.bootstrap.util.ArrayUtils;
import io.arex.agent.bootstrap.util.StringUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.arex.inst.runtime.serializer.Serializer;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ExpressionParseUtil {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final Map<String, Expression> EXPRESSION_MAP = new ConcurrentHashMap<>(32);

    private static final Map<String, String> KEY_FORMULA_MAP = new ConcurrentHashMap<>(32);

    public static String generateKey(Method method, Object[] args, String keyExpression) {
        if (method == null || ArrayUtils.isEmpty(args) || StringUtil.isEmpty(keyExpression)) {
            return null;
        }

        Expression expression = EXPRESSION_MAP.computeIfAbsent(method + keyExpression, key -> {
            try {
                return PARSER.parseExpression(keyExpression);
            } catch (ParseException e) {
                return null;
            }
        });

        if (expression == null) {
            return null;
        }

        try {
            String[] parameterNames = NAME_DISCOVERER.getParameterNames(method);
            if (parameterNames == null || args.length != parameterNames.length) {
                return null;
            }
            EvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
            Object expressionValue = expression.getValue(context);
            if (expressionValue instanceof String) {
                return (String) expressionValue;
            }
            return Serializer.serialize(expressionValue);
        } catch (Exception e) {
            return null;
        }
    }

    public static String replaceToExpression(Method method, String additionalSignature) {
        if (method == null || StringUtil.isEmpty(additionalSignature)) {
            return null;
        }
        if (additionalSignature.contains("#")) {
            return additionalSignature;
        }
        if (!additionalSignature.contains("$")) {
            return null;
        }

        String expression = KEY_FORMULA_MAP.get(method + additionalSignature);
        if (expression != null) {
            return expression;
        }

        try {
            String[] parameterNames = NAME_DISCOVERER.getParameterNames(method);
            if (parameterNames == null || parameterNames.length == 0) {
                return null;
            }

            for (int i = 1; i < parameterNames.length + 1; i++) {
                additionalSignature = StringUtil.replace(additionalSignature, "$" + i, "#" + parameterNames[i - 1]);
            }

            additionalSignature = StringUtil.replace(additionalSignature, "String.", "T(String).");

            KEY_FORMULA_MAP.put(method + additionalSignature, additionalSignature);
            return additionalSignature;
        } catch (Exception e) {
            return null;
        }
    }

}
