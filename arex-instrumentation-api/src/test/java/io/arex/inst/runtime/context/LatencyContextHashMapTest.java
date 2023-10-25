package io.arex.inst.runtime.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

class LatencyContextHashMapTest {
    private static final Map<String, ArexContext> RECORD_MAP = new LatencyContextHashMap();

    @Test
    void test() {
        // null key
        assertNull(RECORD_MAP.get(null));
        assertNull(RECORD_MAP.remove(null));

        String key = "arex-test-id-1";
        // create key
        ArexContext context = RECORD_MAP.computeIfAbsent(key, ArexContext::of);
        assertEquals(key, context.getCaseId());

        // remove key
        context = RECORD_MAP.remove(key);
        assertEquals(key, context.getCaseId());
        assertEquals(0, RECORD_MAP.size());

        // get key again, init latencyMap
        context = RECORD_MAP.get(key);
        assertNull(context);

        // create key2
        String key2 = "arex-test-id-2";
        context = RECORD_MAP.computeIfAbsent(key2, ArexContext::of);
        assertEquals(key2, context.getCaseId());

        // remove key2
        context = RECORD_MAP.remove(key2);
        assertEquals(key2, context.getCaseId());
        assertEquals(0, RECORD_MAP.size());

        context = RECORD_MAP.get(key2);
        assertEquals(key2, context.getCaseId());
    }
}
