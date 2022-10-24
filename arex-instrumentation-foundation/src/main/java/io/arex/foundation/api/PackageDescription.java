package io.arex.foundation.api;

import io.arex.agent.bootstrap.internal.Pair;

public class PackageDescription {

    private Pair<Integer, Integer> from;
    private Pair<Integer, Integer> to;

    private String packageName;

    private PackageDescription(String packageName,
                               Pair<Integer, Integer> supportFrom, Pair<Integer, Integer> supportTo) {
        this.packageName = packageName;
        this.from = supportFrom;
        this.to = supportTo;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isSupported(Pair<Integer, Integer> current) {
        boolean isSupported = current.getFirst() >= from.getFirst() && current.getSecond() >= from.getSecond();
        if (isSupported && to != null) {
            isSupported = current.getFirst() <= to.getFirst() && current.getSecond() <= to.getSecond();
        }
        return isSupported;
    }

    public static final class Builder {
        private String packageName;
        private Pair<Integer, Integer> from;
        private Pair<Integer, Integer> to;

        public Builder with(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder from(int major, int minor) {
            this.from = Pair.of(major, minor);
            return this;
        }

        public Builder to(int major, int minor) {
            this.to = Pair.of(major, minor);
            return this;
        }

        public PackageDescription build() throws Exception {
            if (packageName == null || from == null) {
                throw new Exception("error package version setting.");
            }
            return new PackageDescription(packageName, from, to);
        }
    }
}
