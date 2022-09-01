package io.arex.agent.bootstrap.internal;

import java.util.Objects;

public class Pair<First, Second> {
    private final First first;
    private final Second second;

    private Pair(First first, Second second) {
        this.first = first;
        this.second = second;
    }

    public static <First, Second> Pair<First, Second> of(First first, Second second) {
        return new Pair<>(first, second);
    }

    public First getFirst() {
        return first;
    }

    public Second getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pair && Objects.equals(this.first, ((Pair) obj).first) && Objects
                .equals(this.second, ((Pair) obj).second);
    }

    @Override
    public int hashCode() {
        if (this.first == null) {
            return this.second == null ? 0 : this.second.hashCode() + 1;
        } else {
            return this.second == null ?
                    this.first.hashCode() + 2 :
                    this.first.hashCode() * 17 + this.second.hashCode();
        }
    }

    @Override
    public String toString() {
        return "Pair[" + this.first + "," + this.second + "]";
    }
}
