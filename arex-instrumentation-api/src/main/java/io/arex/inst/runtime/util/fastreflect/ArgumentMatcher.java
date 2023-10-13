package io.arex.inst.runtime.util.fastreflect;

public interface ArgumentMatcher<W, T, R> {
    R apply(W wrapper, T args);
}
