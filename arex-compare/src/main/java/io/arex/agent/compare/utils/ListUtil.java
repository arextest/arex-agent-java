package io.arex.agent.compare.utils;

import java.util.List;

public class ListUtil {

    public static <T> void removeLast(List<T> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        list.remove(list.size() - 1);
    }

}
