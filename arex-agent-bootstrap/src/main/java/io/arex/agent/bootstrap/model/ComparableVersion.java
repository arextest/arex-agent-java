package io.arex.agent.bootstrap.model;

import io.arex.agent.bootstrap.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ComparableVersion implements Comparable<ComparableVersion>{
    private static final char SEPARATOR = '.';
    private static final ComparableVersion EMPTY = new ComparableVersion();
    private final List<Integer> versionItems = new ArrayList<>();

    /**
     * supported format: number.number.number...
     * 1.2.1 -> [1,2,1]
     * 1.2.1-SNAPSHOT -> [1,2,1]
     * 6.2.0.CR1 -> [6,2,0,1]
     */
    public static ComparableVersion of(String version) {
        if (StringUtil.isEmpty(version)) {
            return EMPTY;
        }
        final String[] split = StringUtil.split(version, SEPARATOR);
        List<Integer> list = new ArrayList<>(split.length);
        for (String s : split) {
            if (StringUtil.isNumeric(s)) {
                list.add(Integer.parseInt(s));
            } else {
                list.add(StringUtil.getFirstNumeric(s));
            }
        }
        return new ComparableVersion(list);
    }

    private ComparableVersion() {
    }

    private ComparableVersion(List<Integer> items) {
        this.versionItems.addAll(items);
    }

    public List<Integer> getVersionItems() {
        return versionItems;
    }

    @Override
    public int compareTo(ComparableVersion targetVersion) {
        if (targetVersion == null) {
            return 1;
        }
        Iterator<Integer> currentIterator = this.versionItems.iterator();
        Iterator<Integer> targetIterator = targetVersion.getVersionItems().iterator();
        while (currentIterator.hasNext() && targetIterator.hasNext()) {
            Integer currentPart = currentIterator.next();
            Integer targetPart = targetIterator.next();
            int result = currentPart.compareTo(targetPart);
            if (result != 0) {
                return result;
            }
        }
        if (currentIterator.hasNext()) {
            return 1;
        }
        if (targetIterator.hasNext()) {
            return -1;
        }
        return 0;
    }
}
