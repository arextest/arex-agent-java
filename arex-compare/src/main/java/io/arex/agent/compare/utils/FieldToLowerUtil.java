package io.arex.agent.compare.utils;

import java.util.*;
import java.util.stream.Collectors;

public class FieldToLowerUtil {

    public static List<List<String>> listListToLower(List<List<String>> lists) {
        if (lists == null || lists.isEmpty()) {
            return null;
        }
        List<List<String>> result = new ArrayList<>();
        lists.forEach(item -> {
            result.add(item.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()));
        });
        return result;
    }

    public static Set<String> setToLower(Collection<String> collection) {
        if (collection == null) {
            return null;
        }
        return collection.stream().filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
