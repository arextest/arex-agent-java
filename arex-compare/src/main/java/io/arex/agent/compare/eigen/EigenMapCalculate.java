package io.arex.agent.compare.eigen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.arex.agent.compare.handler.log.filterrules.TimePrecisionFilter;
import io.arex.agent.compare.model.RulesConfig;
import io.arex.agent.compare.utils.IgnoreUtil;
import io.arex.agent.compare.model.eigen.EigenResult;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EigenMapCalculate {

    private static final int OFFSET_BASIS = 0x811C9DC5; // 2166136261
    private static final int FNV_PRIME = 16777619;

    private static TimePrecisionFilter timePrecisionFilter = new TimePrecisionFilter();

    public EigenResult doCalculate(Object obj, RulesConfig rulesConfig) {
        EigenResult eigenResult = new EigenResult();
        Map<Integer, Long> eigenMap = new HashMap<>();
        if (obj == null || obj instanceof String) {
            eigenMap.put(0, this.valueHash(rulesConfig.getBaseMsg()));
            eigenResult.setEigenMap(eigenMap);
            return eigenResult;
        }

        CalculateContext calculateContext = new CalculateContext();
        calculateContext.ignoreNodeSet = rulesConfig.getIgnoreNodeSet();
        calculateContext.exclusions = rulesConfig.getExclusions();
        calculateContext.nodePath = new LinkedList<>();
        doCalculateJsonNode(obj, calculateContext, eigenMap);
        eigenResult.setEigenMap(eigenMap);
        return eigenResult;
    }


    private void doCalculateJsonNode(Object obj, CalculateContext calculateContext,
                                     Map<Integer, Long> eigenMap) {

        // ignore by node name and node path
        if (IgnoreUtil.ignoreProcessor(calculateContext.nodePath, calculateContext.exclusions,
                calculateContext.ignoreNodeSet)) {
            return;
        }

        if (obj instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) obj;
            Iterator<String> stringIterator = objectNode.fieldNames();
            while (stringIterator.hasNext()) {
                String fieldName = stringIterator.next();
                JsonNode jsonNode = objectNode.get(fieldName);

                int lastHash = calculateContext.lastHash;
                calculateContext.lastHash = this.pathHashWithLastHash(fieldName, lastHash);
                calculateContext.nodePath.addLast(fieldName);
                this.doCalculateJsonNode(jsonNode, calculateContext, eigenMap);
                calculateContext.nodePath.removeLast();
                calculateContext.lastHash = lastHash;
            }


        } else if (obj instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) obj;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode jsonNode = arrayNode.get(i);
                int lastHash = calculateContext.lastHash;
                this.doCalculateJsonNode(jsonNode, calculateContext, eigenMap);
                calculateContext.lastHash = lastHash;
            }
        } else {
            // calculate eigen value
            String value = obj == null ? null : obj.toString();

            // process time
            Instant instant = timePrecisionFilter.identifyTime(value);
            if (instant != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        .withZone(ZoneId.systemDefault());
                value = formatter.format(instant);
            }
            int pathHash = calculateContext.lastHash;
            long valueHash = this.valueHash(value);
            eigenMap.put(pathHash, eigenMap.getOrDefault(pathHash, 0L) + valueHash);
        }
    }

    private int pathHashWithLastHash(String nodeName, int lastHash) {
        int key = lastHash;
        for (byte c : nodeName.getBytes()) {
            key = (key ^ c) * FNV_PRIME;
        }
        return Math.abs(key);
    }

    // FNV-1a hash function, think about null and empty string
    private long valueHash(String value) {
        if (value == null) {
            return 1;
        }
        if (value.isEmpty()) {
            return 2;
        }

        int key = OFFSET_BASIS;
        for (byte c : value.getBytes()) {
            key = (key ^ c) * FNV_PRIME;
        }
        key += key << 13;
        key ^= key >> 7;
        key += key << 3;
        key ^= key >> 17;
        key += key << 5;
        return Math.abs(key);
    }


    private static class CalculateContext {

        private LinkedList<String> nodePath;

        private int lastHash = OFFSET_BASIS;

        private Set<String> ignoreNodeSet;
        private List<List<String>> exclusions;

        public CalculateContext() {
        }

        public LinkedList<String> getNodePath() {
            return nodePath;
        }

        public void setNodePath(LinkedList<String> nodePath) {
            this.nodePath = nodePath;
        }

        public int getLastHash() {
            return lastHash;
        }

        public void setLastHash(int lastHash) {
            this.lastHash = lastHash;
        }
    }
}
