package io.arex.inst.httpservlet;

import io.arex.agent.bootstrap.util.CollectionUtil;
import io.arex.agent.bootstrap.util.ReflectUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.pattern.PathPattern;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * The caller should consider the issue of Spring version differences that may throw exceptions.
 * (NoSuchMethodException, NoSuchMethodError, etc.)
 */
public class SpringUtil {

    private static Method getMatchMethod = null;

    private static Method parseAndCacheMethod = null;

    private static ApplicationContext springApplicationContext;

    private static Set<RequestMappingInfo> springRequestMappings;

    public static ApplicationContext getApplicationContext(Object servletContext) {
        if (springApplicationContext != null) {
            return springApplicationContext;
        }
        Method findWebApplicationContextMethod = ReflectUtil.getMethodWithoutClassType(WebApplicationContextUtils.class.getName(),
                "findWebApplicationContext", servletContext.getClass().getInterfaces()[0].getName());
        springApplicationContext = (ApplicationContext) ReflectUtil.invoke(findWebApplicationContextMethod, null, servletContext);
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

    public static String getPatternFromRequestMapping(Object httpServletRequest, Object servletContext) {
        Set<RequestMappingInfo> requestMappingInfos = getAllRequestMappingInfo(servletContext);
        if (CollectionUtil.isEmpty(requestMappingInfos)) {
            return null;
        }

        setPathAttribute(httpServletRequest);

        if (getMatchMethod == null) {
            getMatchMethod = ReflectUtil.getMethodWithoutClassType(RequestMappingInfo.class.getName(),
                    "getMatchingCondition", httpServletRequest.getClass().getInterfaces()[0].getName());
        }

        Set<PathPattern> patterns = new HashSet<>();
        for (RequestMappingInfo mappingInfo : requestMappingInfos) {
            /*
             * execute RequestMappingInfo.getMatchingCondition to get matched pattern
             * eg: /user/lucas/22 -> /user/{name}/{age}
             */
            RequestMappingInfo matchMapping = (RequestMappingInfo) ReflectUtil.invoke(getMatchMethod, mappingInfo, httpServletRequest);
            if (matchMapping != null && matchMapping.getPathPatternsCondition() != null) {
                patterns = matchMapping.getPathPatternsCondition().getPatterns();
                break;
            }
        }

        if (CollectionUtil.isEmpty(patterns)) {
            return null;
        }
        return patterns.iterator().next().getPatternString();
    }

    private static void setPathAttribute(Object httpServletRequest) {
        if (parseAndCacheMethod == null) {
            parseAndCacheMethod = ReflectUtil.getMethodWithoutClassType(ServletRequestPathUtils.class.getName(),
                    "parseAndCache", httpServletRequest.getClass().getInterfaces()[0].getName());
        }
        // execute ServletRequestPathUtils.parseAndCache, because getMatchingCondition need path attribute from request
        ReflectUtil.invoke(parseAndCacheMethod, null, httpServletRequest);
    }
}
