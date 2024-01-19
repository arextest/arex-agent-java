package io.arex.inst.runtime.util.sizeof;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Filter to filter types or fields of object graphs passed to a SizeOf engine
 */
public interface SizeOfFilter {

    /**
     * Returns the fields to walk and measure for a type
     *
     * @param klazz  the type
     * @param fields the fields already "qualified"
     * @return the filtered Set
     */
    Collection<Field> filterFields(Class<?> klazz, Collection<Field> fields);

    /**
     * Checks whether the type needs to be filtered
     *
     * @param klazz the type
     * @return true, if to be filtered out
     */
    boolean filterClass(Class<?> klazz);
}
