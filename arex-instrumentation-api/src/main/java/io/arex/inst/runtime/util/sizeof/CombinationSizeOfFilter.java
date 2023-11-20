package io.arex.inst.runtime.util.sizeof;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Filter combining multiple filters
 *
 * @author Chris Dennis
 */
public class CombinationSizeOfFilter implements SizeOfFilter {

    private final SizeOfFilter[] filters;

    /**
     * Constructs a filter combining multiple ones
     *
     * @param filters the filters to combine
     */
    public CombinationSizeOfFilter(SizeOfFilter... filters) {
        this.filters = filters;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Field> filterFields(Class<?> klazz, Collection<Field> fields) {
        Collection<Field> current = fields;
        for (SizeOfFilter filter : filters) {
            current = filter.filterFields(klazz, current);
        }
        return current;
    }

    /**
     * {@inheritDoc}
     */
    public boolean filterClass(Class<?> klazz) {
        for (SizeOfFilter filter : filters) {
            if (!filter.filterClass(klazz)) {
                return false;
            }
        }
        return true;
    }
}
