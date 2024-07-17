package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.ReflectUtil;
import io.arex.inst.runtime.util.fastreflect.MethodHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * The caller should consider the issue of Spring version differences that may throw exceptions.
 * (NoSuchMethodException, NoSuchMethodError, etc.)
 */
public class SpringUtil {

    private static Method parseAndCacheMethod = null;

    private static ApplicationContext springApplicationContext = null;

    private static Set<RequestMappingInfo> springRequestMappings = null;

    private static final Class<?> SERVLET_REQUEST_PATH_CLASS =
            ReflectUtil.getClass("org.springframework.web.util.ServletRequestPathUtils");

    private static final Method GET_PATTERN_VALUES_METHOD =
            ReflectUtil.getMethodWithoutClassType(RequestMappingInfo.class.getName(), "getPatternValues");

    private static final String SERVLET_REQUEST_JAVAX = "javax.servlet.http.HttpServletRequest";

    private static final String SERVLET_REQUEST_JAKARTA = "jakarta.servlet.http.HttpServletRequest";

    private static final String SERVLET_CONTEXT_JAVAX = "javax.servlet.ServletContext";

    private static final String SERVLET_CONTEXT_JAKARTA = "jakarta.servlet.ServletContext";

    private static MethodHolder<RequestMappingInfo> getMatchMethodHolder = null;

    public static ApplicationContext getApplicationContext(Object servletContext) {
        if (springApplicationContext != null) {
            return springApplicationContext;
        }
        Method getWebApplicationContextMethod = ReflectUtil.getMethodWithoutClassType(WebApplicationContextUtils.class.getName(),
                "getWebApplicationContext", SERVLET_CONTEXT_JAVAX);
        if (getWebApplicationContextMethod == null) {
            getWebApplicationContextMethod = ReflectUtil.getMethodWithoutClassType(WebApplicationContextUtils.class.getName(),
                    "getWebApplicationContext", SERVLET_CONTEXT_JAKARTA);
        }
        springApplicationContext = (ApplicationContext) ReflectUtil.invoke(getWebApplicationContextMethod, null, servletContext);
        return springApplicationContext;
    }

    public static Set<RequestMappingInfo> getAllRequestMappingInfo(Object servletContext) {
        if (springRequestMappings == null) {
            ApplicationContext applicationContext = getApplicationContext(servletContext);
            if (applicationContext == null) {
                return null;
            }
            RequestMappingInfoHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingInfoHandlerMapping.class);
            springRequestMappings = handlerMapping.getHandlerMethods().keySet();
        }
        return springRequestMappings;
    }

    /**
     * By use reflection method to mask the differences between servlet versions, it achieves common use
     * @param httpServletRequest javax.servlet.http.HttpServletRequest or jakarta.servlet.http.HttpServletRequest
     * @param servletContext javax.servlet.ServletContext or jakarta.servlet.ServletContext
     * @return
     */
    public static String getPatternFromRequestMapping(Object httpServletRequest, Object servletContext) {
        Set<RequestMappingInfo> requestMappingInfos = getAllRequestMappingInfo(servletContext);
        if (CollectionUtil.isEmpty(requestMappingInfos)) {
            return null;
        }

        setPathAttribute(httpServletRequest);

        if (getMatchMethodHolder == null) {
            Method getMatchMethod = ReflectUtil.getMethodWithoutClassType(RequestMappingInfo.class.getName(),
                    "getMatchingCondition", SERVLET_REQUEST_JAVAX);
            if (getMatchMethod == null) {
                getMatchMethod = ReflectUtil.getMethodWithoutClassType(RequestMappingInfo.class.getName(),
                        "getMatchingCondition", SERVLET_REQUEST_JAKARTA);
            }
            getMatchMethodHolder = MethodHolder.build(getMatchMethod);
        }

        Set<String> patterns = new HashSet<>();
        PatternsRequestCondition patternsCondition;
        for (RequestMappingInfo mappingInfo : requestMappingInfos) {
            /*
             * fast reflect RequestMappingInfo.getMatchingCondition to get matched pattern
             * eg: /user/lucas/22 -> /user/{name}/{age}
             */
            RequestMappingInfo matchMapping = getMatchMethodHolder.invoke(mappingInfo, httpServletRequest);
            if (matchMapping != null) {
                patternsCondition = matchMapping.getPatternsCondition();
                // getPatternValues is available in Spring 5.3.0+
                if (GET_PATTERN_VALUES_METHOD != null) {
                    patterns.addAll(matchMapping.getPatternValues());
                } else if (patternsCondition != null) {
                    patterns.addAll(patternsCondition.getPatterns());
                }
                break;
            }
        }

        if (CollectionUtil.isEmpty(patterns)) {
            return null;
        }
        return patterns.iterator().next();
    }

    private static void setPathAttribute(Object httpServletRequest) {
        // ServletRequestPathUtils.class is available in Spring 5.3.0+
        if (SERVLET_REQUEST_PATH_CLASS == null) {
            return;
        }
        if (parseAndCacheMethod == null) {
            parseAndCacheMethod = ReflectUtil.getMethodWithoutClassType(ServletRequestPathUtils.class.getName(),
                    "parseAndCache", SERVLET_REQUEST_JAVAX);
            if (parseAndCacheMethod == null) {
                parseAndCacheMethod = ReflectUtil.getMethodWithoutClassType(ServletRequestPathUtils.class.getName(),
                        "parseAndCache", SERVLET_REQUEST_JAKARTA);
            }
        }
        // execute ServletRequestPathUtils.parseAndCache, because getMatchingCondition need path attribute from request
        ReflectUtil.invoke(parseAndCacheMethod, null, httpServletRequest);
    }
}
