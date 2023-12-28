package io.arex.foundation.serializer;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector;
import io.arex.inst.runtime.log.LogManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * When the class does not have a no-argument constructor,
 * create a custom-built no-argument constructor logic
 */
public class ArexObjectMapper extends ObjectMapper {
    private static boolean noCollectMethodWithFiveParameter;
    private static final AtomicBoolean HAS_EXCEPTION = new AtomicBoolean(false);
    static {
        try {
            BasicClassIntrospector.class.getDeclaredMethod("collectProperties",
                    MapperConfig.class, JavaType.class, ClassIntrospector.MixInResolver.class, boolean.class, String.class);
        } catch (Exception e) {
            LogManager.warn("objectMapper.init", e);
            // jackson high version delete this method, example: 2.12.x
            noCollectMethodWithFiveParameter = true;
        }
    }
    @Override
    protected ClassIntrospector defaultClassIntrospector() {
        return new ArexBasicClassIntrospector();
    }

    static class ArexBasicClassIntrospector extends BasicClassIntrospector {
        private static final Class<?> CLS_OBJECT = Object.class;
        private static final Class<?> CLS_STRING = String.class;
        private static final Class<?> CLS_JSON_NODE = JsonNode.class;
        @Override
        public BasicBeanDescription forDeserialization(DeserializationConfig config, JavaType type, MixInResolver r) {
            BasicBeanDescription desc = super.forDeserialization(config, type, r);

            if (HAS_EXCEPTION.get()) {
                return desc;
            }

            if (isMissingDefaultConstructor(type, desc)) {
                try {
                    if (noCollectMethodWithFiveParameter) {
                        return new ArexBasicBeanDescription(collectProperties(config,
                                type, r, false));
                    }
                    return new ArexBasicBeanDescription(collectProperties(config,
                            type, r, false, null));
                } catch (Throwable e) {
                    LogManager.warn("forDeserialization", e);
                    HAS_EXCEPTION.set(true);
                }
            }
            return desc;
        }

        private boolean isMissingDefaultConstructor(JavaType type, BasicBeanDescription desc) {
            if (desc.findDefaultConstructor() != null) {
                return false;
            }
            if (isStdTypeDesc(type)) {
                return false;
            }
            return !super._isStdJDKCollection(type);
        }

        private boolean isStdTypeDesc(JavaType type) {
            Class<?> cls = type.getRawClass();
            if (cls.isPrimitive()) {
                return cls == Integer.TYPE || cls == Long.TYPE || cls == Boolean.TYPE;
            }

            if (cls == CLS_OBJECT || cls == CLS_STRING || cls == Integer.class || cls == Long.class || cls == Boolean.class) {
                return true;
            }

            return CLS_JSON_NODE.isAssignableFrom(cls);
        }
    }

    static class ArexBasicBeanDescription extends BasicBeanDescription {
        private static Constructor<Object> objectConstructor;
        private static Method constructorForSerialization;
        private static Object reflectionFactory;

        static {
            try {
                objectConstructor = Object.class.getDeclaredConstructor();
                Class<?> reflectionFactoryClass = Class.forName("sun.reflect.ReflectionFactory");
                Method getReflectionFactoryMethod = reflectionFactoryClass.getDeclaredMethod("getReflectionFactory");
                reflectionFactory = getReflectionFactoryMethod.invoke(null);

                Class<?>[] parameterTypes = {Class.class, Constructor.class};
                constructorForSerialization = reflectionFactoryClass.getDeclaredMethod("newConstructorForSerialization", parameterTypes);
            } catch (Exception e) {
                LogManager.warn("ArexBasicBeanDescription", e);
            }
        }

        public ArexBasicBeanDescription(POJOPropertiesCollector coll) {
            super(coll);
        }

        @Override
        public AnnotatedConstructor findDefaultConstructor() {
            AnnotatedConstructor annotatedConstructor = super.findDefaultConstructor();
            if (annotatedConstructor == null) {
                try {
                    // no default constructor, create one for deserialize
                    Constructor<?> defaultConstructor =
                            (Constructor<?>) constructorForSerialization.invoke(reflectionFactory, this.getBeanClass(), objectConstructor);
                    annotatedConstructor = new
                            AnnotatedConstructor(null, defaultConstructor, null, null);
                } catch (Throwable ex) {
                    LogManager.warn("findDefaultConstructor", ex);
                    HAS_EXCEPTION.set(true);
                }
            }
            return annotatedConstructor;
        }
    }

}
