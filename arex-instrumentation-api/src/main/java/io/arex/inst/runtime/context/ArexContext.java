package io.arex.inst.runtime.context;

import io.arex.agent.bootstrap.cache.TimeCache;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.model.Constants;
import io.arex.inst.runtime.model.DynamicClassMocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArexContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArexContext.class);
    private final String caseId;
    private final String replayId;
    private final long createTime;
    private final SequenceProvider sequence;
    private final List<Integer> methodSignatureHashList = new ArrayList<>();
    private final Map<String, Object> cachedReplayResultMap = new ConcurrentHashMap<>();
    private Map<String, Set<String>> excludeMockTemplate;

    public String getCaseId() {
        return this.caseId;
    }

    public String getReplayId() {
        return this.replayId;
    }

    public long getCreateTime() {
        return createTime;
    }

    private ArexContext(String caseId, String replayId) {
        this.createTime = System.currentTimeMillis();
        this.caseId = caseId;
        this.sequence = new SequenceProvider();
        this.replayId = replayId;

        System.out.println("[AREX] ArexContext classloader:" + this.getClass().getClassLoader());

        //init();
    }

    public boolean isReplay() {
        return StringUtil.isNotEmpty(this.replayId);
    }

    public void add(String key, String value) {

    }

    public String get(String key) {
        return null;
    }

    public int calculateSequence(String target) {
        return StringUtil.isEmpty(target) ? 0 : sequence.get(target);
    }

    public int calculateSequence() {
        return 0;
    }

    public static ArexContext of(String caseId) {
        return of(caseId, null);
    }

    public static ArexContext of(String caseId, String replayId) {
        return new ArexContext(caseId, replayId);
    }

    private void init() {
        System.out.println("[AREX] ArexContext init enter.");
        TimeCache.remove();
        try {
            if (StringUtil.isNotEmpty(replayId) && Config.get().getBoolean("arex.time.machine", false)) {
                DynamicClassMocker mocker = new DynamicClassMocker(Constants.CLOCK_CLASS, Constants.CLOCK_METHOD, null);
                Object result = mocker.replay();
                long millis = Long.parseLong(String.valueOf(result));
                if (millis > 0) {
                    TimeCache.put(millis);
                }
            } else {
                DynamicClassMocker mocker = new DynamicClassMocker(Constants.CLOCK_CLASS, Constants.CLOCK_METHOD,
                        null, String.valueOf(System.currentTimeMillis()), Long.class.getName());
                mocker.record();
            }
        } catch (Throwable e) {
            LOGGER.warn("ArexContext init failed.", e);
        }
        System.out.println("[AREX] ArexContext init exit.");
    }

    public List<Integer> getMethodSignatureHashList() {
        return methodSignatureHashList;
    }

    public Map<String, Object> getCachedReplayResultMap() {
        return cachedReplayResultMap;
    }
    public Map<String, Set<String>> getExcludeMockTemplate() {
        return excludeMockTemplate;
    }

    public void setExcludeMockTemplate(Map<String, Set<String>> excludeMockTemplate) {
        this.excludeMockTemplate = excludeMockTemplate;
    }
    public void clear() {
        methodSignatureHashList.clear();
        cachedReplayResultMap.clear();
        sequence.clear();
        if (excludeMockTemplate != null) {
            excludeMockTemplate.clear();
        }
    }
}
