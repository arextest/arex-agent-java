package io.arex.inst.runtime.util.sizeof;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

public class ThrowableFilter implements SizeOfFilter {
    public static final ThrowableFilter INSTANCE = new ThrowableFilter();
    @Override
    public Collection<Field> filterFields(Class<?> klazz, Collection<Field> fields) {
        return !Throwable.class.isAssignableFrom(klazz) ? fields : Collections.emptyList();
    }

    @Override
    public boolean filterClass(Class<?> klazz) {
        return !Throwable.class.isAssignableFrom(klazz);
    }
}
