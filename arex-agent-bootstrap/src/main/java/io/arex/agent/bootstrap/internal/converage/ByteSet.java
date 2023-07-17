package io.arex.agent.bootstrap.internal.converage;

class ByteSet {

    private static final byte[] EMPTY_ARRAY = new byte[0];
    private transient byte[] a;
    private int size;
    public ByteSet() {
        this.a = EMPTY_ARRAY;
    }

    public ByteSet(final int capacity) {
        this.a = new byte[capacity];
    }

    private int findKey(final byte o) {
        for (int i = size; i-- != 0;)
            if (((a[i]) == (o)))
                return i;
        return -1;
    }

    public boolean contains(final byte k) {
        return findKey(k) != -1;
    }

    public int size() {
        return size;
    }

    public boolean add(final byte k) {
        final int pos = findKey(k);
        if (pos != -1)
            return false;
        if (size == a.length) {
            final byte[] b = new byte[size == 0 ? 2 : size * 2];
            for (int i = size; i-- != 0;)
                b[i] = a[i];
            a = b;
        }
        a[size++] = k;
        return true;
    }

    public void clear() {
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int hashCode() {
        int h = 0;
        for (int i = size, j = 0; i-- != 0;) {
            h += (a[i]);
        }
        return h;
    }

    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ByteSet))
            return false;
        ByteSet s = (ByteSet) o;
        return s.size == size && java.util.Arrays.equals(a, s.a);
    }
}
