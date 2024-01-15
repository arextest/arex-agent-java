package io.arex.inst.runtime.util.sizeof;

import io.arex.agent.bootstrap.internal.WeakCache;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;

import static java.util.Collections.newSetFromMap;

public class ObjectGraphWalker {
    private final WeakCache<Class<?>, SoftReference<Collection<Field>>> fieldCache = new WeakCache<>();
    private final WeakCache<Class<?>, Boolean> classCache = new WeakCache<>();
    private final SizeOfFilter sizeOfFilter;
    private final Visitor visitor;

    /**
     * Constructor
     *
     * @param visitor the visitor to use
     * @param filter  the filtering
     * @see Visitor
     * @see SizeOfFilter
     */
    ObjectGraphWalker(Visitor visitor, SizeOfFilter filter) {
        if(visitor == null) {
            throw new NullPointerException("Visitor can't be null");
        }
        if(filter == null) {
            throw new NullPointerException("SizeOfFilter can't be null");
        }
        this.visitor = visitor;
        this.sizeOfFilter = filter;
    }

    /**
     * The visitor to execute the function on each node of the graph
     * This is only to be used for the sizing of an object graph in memory!
     */
    interface Visitor {
        /**
         * The visit method executed on each node
         *
         * @param object the reference at that node
         * @return a long for you to do things with...
         */
        long visit(Object object);
    }

    /**
     * Walk the graph and call into the "visitor"
     *
     * @param root                      the roots of the objects (a shared graph will only be visited once)
     * @return the sum of all Visitor#visit returned values
     */
    long walk(Object... root) {
        return walk(null, root);
    }

    /**
     * Walk the graph and call into the "visitor"
     *
     * @param visitorListener          A decorator for the Visitor
     * @param root                      the roots of the objects (a shared graph will only be visited once)
     * @return the sum of all Visitor#visit returned values
     */
    long walk(VisitorListener visitorListener, Object... root) {
        long result = 0;
        Deque<Object> toVisit = new ArrayDeque<>();
        Set<Object> visited = newSetFromMap(new IdentityHashMap<>());

        nullSafeAddArray(toVisit, root);

        while (!toVisit.isEmpty()) {
            Object ref = toVisit.pop();
            if (!visited.add(ref) || !shouldWalkClass(ref.getClass())) {
                continue;
            }

            walkField(ref, toVisit);

            final long visitSize = visitor.visit(ref);
            if (visitorListener != null) {
                visitorListener.visited(ref, visitSize);
            }
            result += visitSize;

        }

        return result;
    }

    private void walkField(Object ref, Deque<Object> toVisit) {
        Class<?> refClass = ref.getClass();
        if (refClass.isArray() && !refClass.getComponentType().isPrimitive()) {
            for (int i = 0; i < Array.getLength(ref); i++) {
                nullSafeAdd(toVisit, Array.get(ref, i));
            }
            return;
        }
        for (Field field : getFilteredFields(refClass)) {
            try {
                nullSafeAdd(toVisit, field.get(ref));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Returns the filtered fields for a particular type
     *
     * @param refClass the type
     * @return A collection of fields to be visited
     */
    private Collection<Field> getFilteredFields(Class<?> refClass) {
        SoftReference<Collection<Field>> ref = fieldCache.get(refClass);
        Collection<Field> fieldList = ref != null ? ref.get() : null;
        if (fieldList != null) {
            return fieldList;
        } else {
            Collection<Field> result;
            result = sizeOfFilter.filterFields(refClass, getAllFields(refClass));
            fieldCache.put(refClass, new SoftReference<>(result));
            return result;
        }
    }

    private boolean shouldWalkClass(Class<?> refClass) {
        Boolean cached = classCache.get(refClass);
        if (cached == null) {
            cached = sizeOfFilter.filterClass(refClass);
            classCache.put(refClass, cached);
        }
        return cached;
    }

    private static void nullSafeAddArray(final Deque<Object> toVisit, final Object... root) {
        if (root != null) {
            for (Object object : root) {
                nullSafeAdd(toVisit, object);
            }
        }
    }

    private static void nullSafeAdd(final Deque<Object> toVisit, final Object o) {
        if (o != null) {
            toVisit.push(o);
        }
    }

    /**
     * Returns all non-primitive fields for the entire class hierarchy of a type
     *
     * @param refClass the type
     * @return all fields for that type
     */
    private static Collection<Field> getAllFields(Class<?> refClass) {
        Collection<Field> fields = new ArrayList<>();
        for (Class<?> klazz = refClass; klazz != null; klazz = klazz.getSuperclass()) {
            for (Field field : klazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive()) {
                    try {
                        field.setAccessible(true);
                    } catch (RuntimeException e) {
                        continue;
                    }
                    fields.add(field);
                }
            }
        }
        return fields;
    }
}
