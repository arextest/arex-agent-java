package io.arex.inst.runtime.util.sizeof;

public interface VisitorListener {
    void visited(final Object object, final long size);
}
