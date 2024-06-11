package io.arex.agent.bootstrap.util;

import java.util.*;

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

    @SafeVarargs
    public static <E> Set<E> newHashSet(E... elements) {
        if (elements == null) {
            return new HashSet<>();
        }

        Set<E> set = new HashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * split to multiple list by split count
     */
    public static <V> List<List<V>> split(List<V> originalList, int splitCount) {
        if (isEmpty(originalList)) {
            return emptyList();
        }
        int originalSize = originalList.size();
        List<List<V>> splitList = new ArrayList<>();
        if (originalSize < splitCount || splitCount == 0) {
            splitList.add(originalList);
            return splitList;
        }
        for (int i = 0; i < splitCount; i++) {
            List<V> list = new ArrayList<>();
            splitList.add(list);
        }
        int index = 0;
        for (V value : originalList) {
            splitList.get(index).add(value);
            index = (index + 1) % splitCount;
        }
        return splitList;
    }

    public static <V> List<V> filterNull(List<V> originalList) {
        if (isEmpty(originalList)) {
            return emptyList();
        }
        List<V> filterList = new ArrayList<>(originalList.size());
        for (V element : originalList) {
            if (element != null) {
                filterList.add(element);
            }
        }
        return filterList;
    }
}
