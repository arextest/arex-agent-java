package io.arex.foundation.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseExceptionMockUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseExceptionMockUtil.class);
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>(10);
    private static final Map<String, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(10);
    private static final Class<?>[] PARAMETER_WITH_STRING = new Class<?>[] {String.class};
    private static final Class<?>[] PARAMETER_WITH_STRING_THROWABLE = new Class<?>[] {String.class, Throwable.class};
    private static final String EXCEPTION_SEPARATOR = "{EX}";
    private static final String CONST_THROWABLE = "Throwable";
    private static final String CONST_NULL = "null";

    /**
     * Format exception when record
     */
    public static String formatResponseException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(50);
        buildExceptionInfo(builder, throwable);

        if (throwable.getCause() != null) {
            buildExceptionInfo(builder, throwable.getCause());
        }

        return builder.toString();
    }

    /**
     * Generate mock exception when replay
     */
    public static Throwable generateMockException(String response) {
        Map<String, String> exceptionMap = parseExceptionInfo(response);

        if (exceptionMap == null || exceptionMap.size() == 0) {
            return null;
        }

        Throwable innerException = null;
        Throwable rootException = null;
        for (Map.Entry<String, String> entry : exceptionMap.entrySet()) {
            if (innerException == null) {
                innerException = generateThrowable(entry.getKey(), entry.getValue(), null);
            } else {
                rootException = generateThrowable(entry.getKey(), entry.getValue(), innerException);
            }
        }
        // Return a new exception when generate throwable fails, avoid deserialize the result
        if (innerException == null) {
            return new Exception("arex record throwable, but can't get constructor on replay.");
        }
        return rootException != null ? rootException : innerException;
    }

    /**
     * Use {EX} as a delimiter, avoid exception message contains =
     * example: {EX}org.apache.dubbo.rpc.RpcException{EX}aaa{EX}java.lang.NullPointerException{EX}bbb
     */
    private static void buildExceptionInfo(StringBuilder builder, Throwable throwable) {
        builder.append(EXCEPTION_SEPARATOR).append(throwable.getClass().getTypeName());
        builder.append(EXCEPTION_SEPARATOR).append(throwable.getMessage());
    }

    /**
     * Parse exception in reverse order
     */
    private static Map<String, String> parseExceptionInfo(String exceptionInfo) {
        if (StringUtil.isEmpty(exceptionInfo)) {
            return null;
        }

        if (exceptionInfo.startsWith("\"") && exceptionInfo.endsWith("\"")) {
            exceptionInfo = exceptionInfo.substring(1, exceptionInfo.length() - 1);
        }

        String[] exceptionArray = StringUtils.splitByWholeSeparator(exceptionInfo, EXCEPTION_SEPARATOR);
        Map<String, String> map = new HashMap<>(exceptionArray.length / 2);
        // exception class-message appeared in pairs
        int index;
        for (index = exceptionArray.length - 2; index >= 0; index -= 2) {
            map.put(exceptionArray[index], exceptionArray[index + 1]);
        }
        return map;
    }

    private static Throwable generateThrowable(String className, String message, Throwable innerException) {
        Throwable throwable = null;
        try {
            Class<?> clazz = getCachedClass(className);
            if (clazz == null) {
                return null;
            }

            Constructor<?> constructor = getCachedConstructor(clazz, className, innerException);
            if (constructor == null) {
                return null;
            }

            String exMessage = CONST_NULL.equals(message) ? null : message;
            if (innerException == null) {
                throwable = (Throwable) constructor.newInstance(exMessage);
            } else {
                throwable = (Throwable) constructor.newInstance(exMessage, innerException);
            }
        } catch (Exception e) {
            LOGGER.warn(LogUtil.buildTitle("generateThrowable"), e);
        }

        return throwable;
    }

    private static Class<?> tryLoadClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        }
    }

    private static Class<?> getCachedClass(String className) {
        return CLASS_CACHE.computeIfAbsent(className, key -> {
            try {
                return tryLoadClass(className);
            } catch (ClassNotFoundException e) {
                LOGGER.warn(LogUtil.buildTitle("getCachedClass"), e);
                return null;
            }
        });
    }

    private static Constructor<?> getCachedConstructor(Class<?> clazz, String className, Throwable innerException) {
        if (innerException == null) {
            return getCachedConstructor(clazz, className, PARAMETER_WITH_STRING);
        }

        String classKey = className + CONST_THROWABLE;

        return getCachedConstructor(clazz, classKey, PARAMETER_WITH_STRING_THROWABLE);
    }

    private static Constructor<?> getCachedConstructor(Class<?> clazz, String className, Class<?>[] parameters) {
        return CONSTRUCTOR_CACHE.computeIfAbsent(className, key -> {
            try {
                for (Constructor<?> ctor: clazz.getDeclaredConstructors()) {
                    if (Arrays.equals(ctor.getParameterTypes(), parameters)) {
                        if (!Modifier.isPublic(ctor.getModifiers())) {
                            ctor.setAccessible(true);
                        }
                        return ctor;
                    }
                }
            } catch (Exception e) {
                LOGGER.warn(LogUtil.buildTitle("getCachedConstructor"), e);
            }
            return null;
        });
    }
}
