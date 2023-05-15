package io.arex.agent.bootstrap.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionUtil {
    private static final List<?> EMPTY_LIST = newArrayList();


    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private CollectionUtil() {}

    public static boolean isEmpty(Collection<?> coll) {
        return (coll == null || coll.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    @SuppressWarnings("unchecked")
    public static <E> List<E> emptyList() {
        return (List<E>) EMPTY_LIST;
    }

    public static <E> List<E> newArrayList() {
        return new ArrayList<>();
    }

    @SafeVarargs
    public static <E> List<E> newArrayList(E... elements) {
        if (elements == null) {
            return emptyList();
        }

        List<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }
}
