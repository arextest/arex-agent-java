package io.arex.agent.bootstrap.ctx;

import io.arex.agent.bootstrap.internal.CallDepth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * transmittable-thread-local simple version
 * 1. Avoid too many package dependencies
 * 2. Support more ThreadPool（agent）
 * 3. transmittable-thread-local not work in nio.reactor.Worker（@see AbstractMultiworkerIOReactor）
 * 4. Change from InheritableThreadLocal to ThreadLocal，avoid collect unexpected data
 */
public class ArexThreadLocal<T> extends ThreadLocal<T> {

    public ArexThreadLocal() {
    }

    @Override
    public final T get() {
        T value = super.get();
        if (null != value) addThisToHolder();
        return value;
    }

    @Override
    public final void set(T value) {
        if (null == value) {
            remove();
        } else {
            super.set(value);
            addThisToHolder();
        }
    }

    @Override
    public final void remove() {
        removeThisFromHolder();
        super.remove();
    }

    public void superRemove() {
        super.remove();
    }

    public T copyValue() {
        T value = get();
        if (value instanceof CallDepth) {
            return (T) ((CallDepth) value).copy();
        }
        return value;
    }

    private static final ThreadLocal<WeakHashMap<ArexThreadLocal<Object>, ?>> holder =
            new InheritableThreadLocal<WeakHashMap<ArexThreadLocal<Object>, ?>>() {
                @Override
                protected WeakHashMap<ArexThreadLocal<Object>, ?> initialValue() {
                    return new WeakHashMap<>();
                }

                @Override
                protected WeakHashMap<ArexThreadLocal<Object>, ?> childValue(WeakHashMap<ArexThreadLocal<Object>, ?> parentValue) {
                    return new WeakHashMap<ArexThreadLocal<Object>, Object>(parentValue);
                }
            };

    @SuppressWarnings("unchecked")
    private void addThisToHolder() {
        if (!holder.get().containsKey(this)) {
            holder.get().put((ArexThreadLocal<Object>) this, null);
        }
    }

    private void removeThisFromHolder() {
        holder.get().remove(this);
    }

    public static class Transmitter {

        public static Object capture() {
            HashMap<ArexThreadLocal<Object>, Object> values = captureValues();
            return values == null ? null : new Snapshot(values);
        }

        private static HashMap<ArexThreadLocal<Object>, Object> captureValues() {
            HashMap<ArexThreadLocal<Object>, Object> values = null;
            for (ArexThreadLocal<Object> threadLocal : holder.get().keySet()) {
                if (null == values) {
                    values = new HashMap<>();
                }
                values.put(threadLocal, threadLocal.copyValue());
            }
            return values;
        }

        public static Object replay(Object captured) {
            if (captured == null) {
                return null;
            }

            final Snapshot capturedSnapshot = (Snapshot) captured;
            return new Snapshot(replayValues(capturedSnapshot.values));
        }

        private static HashMap<ArexThreadLocal<Object>, Object> replayValues(HashMap<ArexThreadLocal<Object>, Object> captured) {
            HashMap<ArexThreadLocal<Object>, Object> backup = new HashMap<>();
            for (final Iterator<ArexThreadLocal<Object>> iterator = holder.get().keySet().iterator(); iterator.hasNext(); ) {
                ArexThreadLocal<Object> threadLocal = iterator.next();
                backup.put(threadLocal, threadLocal.get());
                if (!captured.containsKey(threadLocal)) {
                    iterator.remove();
                    threadLocal.superRemove();
                }
            }

            setTtlTo(captured);
            return backup;
        }

        public static void restore(Object backup) {
            if (backup == null) {
                return;
            }

            final Snapshot backupSnapshot = (Snapshot) backup;
            restoreValues(backupSnapshot.values);
        }

        private static void restoreValues(HashMap<ArexThreadLocal<Object>, Object> backup) {
            for (final Iterator<ArexThreadLocal<Object>> iterator = holder.get().keySet().iterator(); iterator.hasNext(); ) {
                ArexThreadLocal<Object> threadLocal = iterator.next();
                if (!backup.containsKey(threadLocal)) {
                    iterator.remove();
                    threadLocal.superRemove();
                }
            }
            setTtlTo(backup);
        }

        public static void setTtlTo(HashMap<ArexThreadLocal<Object>, Object> ttlValues) {
            for (Map.Entry<ArexThreadLocal<Object>, Object> entry : ttlValues.entrySet()) {
                ArexThreadLocal<Object> threadLocal = entry.getKey();
                threadLocal.set(entry.getValue());
            }
        }

        public static class Snapshot {
            final HashMap<ArexThreadLocal<Object>, Object> values;

            public Snapshot(HashMap<ArexThreadLocal<Object>, Object> values) {
                this.values = values;
            }
        }

        private Transmitter() {

        }
    }

}
