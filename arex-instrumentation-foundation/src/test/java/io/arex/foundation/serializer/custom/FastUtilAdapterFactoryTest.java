package io.arex.foundation.serializer.custom;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.foundation.serializer.GsonSerializer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class FastUtilAdapterFactoryTest {
    public static TestType getTestType() {
        final TestType testType = new TestType(1, "test");
        final IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
        intOpenHashSet.add(2);
        testType.setIntSet(intOpenHashSet);
        final FloatArrayList floats = new FloatArrayList();
        floats.add(1.0f);
        testType.setFloats(floats);
        final LongLinkedOpenHashSet linkedOpenHashSet = new LongLinkedOpenHashSet();
        linkedOpenHashSet.add(3L);
        testType.setLongSet(linkedOpenHashSet);
        final HashSet<Integer> hashSet = new HashSet<>();
        hashSet.add(1);
        testType.setSet(new Set[]{hashSet});
        return testType;
    }

    @Test
    void testFastUtilAdapter() throws Throwable {
        final TestType testType = getTestType();
        final String json = GsonSerializer.INSTANCE.serialize(testType);
        final TestType deserializeTestType = GsonSerializer.INSTANCE.deserialize(json, TestType.class);
        assertNotNull(deserializeTestType);
    }

    public static class TestType {
        private int id;
        private String name;
        private IntSet intSet;
        private FloatList floats;
        private LongSet longSet;
        private Set<Integer>[] set;

        public TestType() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public TestType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public IntSet getIntSet() {
            return intSet;
        }

        public void setIntSet(IntSet intSet) {
            this.intSet = intSet;
        }

        public FloatList getFloats() {
            return floats;
        }

        public void setFloats(FloatList floats) {
            this.floats = floats;
        }

        public LongSet getLongSet() {
            return longSet;
        }

        public void setLongSet(LongSet longSet) {
            this.longSet = longSet;
        }

        public Set<Integer>[] getSet() {
            return set;
        }

        public void setSet(Set<Integer>[] set) {
            this.set = set;
        }
    }
}