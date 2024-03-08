package io.arex.inst.cache.util;

import com.google.common.cache.CacheLoader;
import io.arex.inst.cache.common.CacheLoaderUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CacheLoaderUtilTest {
    private static final CacheLoader cacheLoader = new CacheLoader<String, Object>() {
        @Override
        public Object load(String key) throws Exception {
            return "test";
        }
    };

    @Test
    void getLocatedClass() throws Exception {
        Field noReferenceMap = CacheLoaderUtil.class.getDeclaredField("NO_REFERENCE_MAP");
        noReferenceMap.setAccessible(true);
        Map<Integer, String> NO_REFERENCE_MAP = (Map<Integer, String>) noReferenceMap.get(null);
        Field referenceFieldMap = CacheLoaderUtil.class.getDeclaredField("REFERENCE_FIELD_MAP");
        referenceFieldMap.setAccessible(true);
        Map<Integer, Field> REFERENCE_FIELD_MAP = (Map<Integer, Field>) referenceFieldMap.get(null);

        // loader is null
        String locatedClass = CacheLoaderUtil.getLocatedClass(null);
        assertEquals("", locatedClass);
        assertEquals(0, NO_REFERENCE_MAP.size());
        assertEquals(0, REFERENCE_FIELD_MAP.size());

        // loader location is not abstract or interface
        locatedClass = CacheLoaderUtil.getLocatedClass(cacheLoader);
        assertEquals("io.arex.inst.cache.util.CacheLoaderUtilTest$1", locatedClass);
        assertEquals(1, NO_REFERENCE_MAP.size());
        assertEquals(0, REFERENCE_FIELD_MAP.size());

        // get from cache
        locatedClass = CacheLoaderUtil.getLocatedClass(cacheLoader);
        assertEquals("io.arex.inst.cache.util.CacheLoaderUtilTest$1", locatedClass);

        // loader location is abstract or interface
        CacheLoader abstractClasscacheLoader = new SubCache1().abstractClasscacheLoader;
        locatedClass = CacheLoaderUtil.getLocatedClass(abstractClasscacheLoader);
        assertEquals("io.arex.inst.cache.util.CacheLoaderUtilTest$SubCache1", locatedClass);
        assertEquals(1, NO_REFERENCE_MAP.size());
        assertEquals(1, REFERENCE_FIELD_MAP.size());

        // get from cache
        locatedClass = CacheLoaderUtil.getLocatedClass(abstractClasscacheLoader);
        assertEquals("io.arex.inst.cache.util.CacheLoaderUtilTest$SubCache1", locatedClass);

        locatedClass = CacheLoaderUtil.getLocatedClass(AbstractCache2.cacheLoader);
        assertEquals("io.arex.inst.cache.util.CacheLoaderUtilTest$AbstractCache2$1", locatedClass);
        assertEquals(2, NO_REFERENCE_MAP.size());
        assertEquals(1, REFERENCE_FIELD_MAP.size());

    }


    static abstract class AbstractCache {
        public final CacheLoader abstractClasscacheLoader = new CacheLoader<String, Object>() {
            @Override
            public Object load(String key) throws Exception {
                return "test";
            }
        };
    }

    static class SubCache1 extends AbstractCache {
    }

    static abstract class AbstractCache2 {
        public static final CacheLoader cacheLoader = new CacheLoader<String, Object>() {
            @Override
            public Object load(String key) throws Exception {
                return "test";
            }
        };
    }

}
