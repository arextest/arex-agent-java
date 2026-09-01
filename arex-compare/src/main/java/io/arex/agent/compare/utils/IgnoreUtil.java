package io.arex.agent.compare.utils;

import io.arex.agent.compare.model.enumeration.Constant;

import java.util.List;
import java.util.Set;

/**
 * Created by rchen9 on 2022/9/22.
 */
public class IgnoreUtil {

    public static boolean ignoreProcessor(List<String> nodePath, List<List<String>> ignoreNodePaths,
                                          Set<String> ignoreNodeSet) {
        if (ignoreNodeProcessor(nodePath, ignoreNodeSet)) {
            return true;
        }

        if (ignoreNodePaths != null && !ignoreNodePaths.isEmpty()) {
            for (List<String> ignoreNodePath : ignoreNodePaths) {
                if (ignoreMatch(nodePath, ignoreNodePath)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean ignoreMatch(List<String> pathInList, List<String> ignoreNodePath) {

        int size = ignoreNodePath.size();
        if (size > pathInList.size()) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (!ignoreNodePath.get(i).equals(pathInList.get(i)) && !ignoreNodePath.get(i)
                    .equals(Constant.DYNAMIC_PATH)) {
                return false;
            }
        }
        return true;
    }

    private static boolean ignoreNodeProcessor(List<String> nodePath, Set<String> ignoreNodeSet) {

        if (ignoreNodeSet == null || ignoreNodeSet.isEmpty()) {
            return false;
        }

        if (nodePath == null || nodePath.isEmpty()) {
            return false;
        }

        for (String nodeName : nodePath) {
            if (ignoreNodeSet.contains(nodeName)) {
                return true;
            }
        }
        return false;
    }
}
