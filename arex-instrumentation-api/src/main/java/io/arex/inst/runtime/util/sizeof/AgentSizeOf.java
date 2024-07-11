package io.arex.inst.runtime.util.sizeof;

import io.arex.agent.bootstrap.InstrumentationHolder;
import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.log.LogManager;
import io.arex.inst.runtime.model.ArexConstants;

import java.lang.instrument.Instrumentation;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * refer: https://github.com/ehcache/sizeof
 */
public class AgentSizeOf {
    /** One kilobyte bytes. */
    public static final long ONE_KB = 1024;
    /** One megabyte bytes. */
    public static final long ONE_MB = ONE_KB * ONE_KB;
    /** One gigabyte bytes.*/
    public static final long ONE_GB = ONE_KB * ONE_MB;
    private final Instrumentation instrumentation;
    private final ObjectGraphWalker walker;
    private AgentSizeOf(SizeOfFilter fieldFilter) {
        this.instrumentation = InstrumentationHolder.getInstrumentation();
        ObjectGraphWalker.Visitor visitor = new SizeOfVisitor();
        this.walker = new ObjectGraphWalker(visitor, fieldFilter);
    }

    /**
     * Measures the size in memory (heap) of the objects passed in, walking their graph down
     * Any overlap of the graphs being passed in will be recognized and only measured once
     *
     * @return the total size in bytes for these objects
     */
    public long deepSizeOf(long sizeThreshold, Object... obj) {
        return walker.walk(null, sizeThreshold, obj);
    }

    public static AgentSizeOf newInstance(final SizeOfFilter... filters) {
        final SizeOfFilter filter = new CombinationSizeOfFilter(filters);
        return new AgentSizeOf(filter);
    }

    /**
     * Will return the sizeOf each instance
     */
    private class SizeOfVisitor implements ObjectGraphWalker.Visitor {
        public long visit(Object object) {
            return sizeOf(object);
        }
    }

    public long sizeOf(Object obj) {
        if (instrumentation == null) {
            return 0;
        }
        return instrumentation.getObjectSize(obj);
    }

    /**
     * Returns <code>size</code> in human-readable units (GB, MB, KB or bytes).
     */
    public static String humanReadableUnits(long bytes) {
        return humanReadableUnits(bytes,
                new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT)));
    }

    /**
     * Returns <code>size</code> in human-readable units (GB, MB, KB or bytes).
     */
    public static String humanReadableUnits(long bytes, DecimalFormat df) {
        if (bytes / ONE_GB > 0) {
            return df.format((float) bytes / ONE_GB) + " GB";
        } else if (bytes / ONE_MB > 0) {
            return df.format((float) bytes / ONE_MB) + " MB";
        } else if (bytes / ONE_KB > 0) {
            return df.format((float) bytes / ONE_KB) + " KB";
        } else {
            return bytes + " bytes";
        }
    }

    /**
     * @return true: not exceed memory size limit
     */
    public boolean checkMemorySizeLimit(Object obj, long sizeLimit) {
        long start = System.currentTimeMillis();
        long memorySize = deepSizeOf(sizeLimit, obj);
        long cost = System.currentTimeMillis() - start;
        if (cost > 50) { // used statistical performance can be removed in the future
            LogManager.info("check.memory.size",
                    StringUtil.format("size: %s, cost: %s ms",
                    humanReadableUnits(memorySize), String.valueOf(cost)));
        }
        return memorySize < sizeLimit;
    }

    public static long getSizeLimit() {
        return Config.get().getLong(ArexConstants.RECORD_SIZE_LIMIT, ArexConstants.MEMORY_SIZE_1MB);
    }
}
