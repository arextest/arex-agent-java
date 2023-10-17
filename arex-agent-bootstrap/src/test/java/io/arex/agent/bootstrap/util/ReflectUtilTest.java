package io.arex.agent.bootstrap.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReflectUtilTest {

    @Test
    void getFieldOrInvokeMethod() throws Exception {
        assertEquals(0, ReflectUtil.getFieldOrInvokeMethod(() -> String.class.getDeclaredField("hash"),"mock"));
        assertNull(ReflectUtil.getFieldOrInvokeMethod(() -> System.class.getDeclaredMethod("checkIO"), null));
    }

    @Test
    void getCollectionInstance() {
        Collection<?> actualResult = ReflectUtil.getCollectionInstance("java.util.ArrayList");
        assertInstanceOf(ArrayList.class, actualResult);

        actualResult = ReflectUtil.getCollectionInstance("java.util.LinkedList");
        assertInstanceOf(LinkedList.class, actualResult);

        actualResult = ReflectUtil.getCollectionInstance("java.util.Collections$EmptyList");
        assertInstanceOf(Collections.emptyList().getClass(), actualResult);

        actualResult = ReflectUtil.getCollectionInstance("java.util.HashSet");
        assertInstanceOf(HashSet.class, actualResult);

        actualResult = ReflectUtil.getCollectionInstance("java.util.LinkedHashSet");
        assertInstanceOf(LinkedHashSet.class, actualResult);

        actualResult = ReflectUtil.getCollectionInstance("java.util.TreeSet");
        assertInstanceOf(TreeSet.class, actualResult);

        actualResult = ReflectUtil.getCollectionInstance("java.util.Collections$EmptySet");
        assertInstanceOf(Collections.emptySet().getClass(), actualResult);

        actualResult = ReflectUtil.getCollectionInstance("java.util.ArrayDeque");
        assertInstanceOf(ArrayDeque.class, actualResult);

        actualResult = ReflectUtil.getCollectionInstance("java.util.HashMap");
        assertNull(actualResult);
    }

    @Test
    void getMethod() {
        assertNotNull(ReflectUtil.getMethod(String.class, "indexOf", int.class, 2));
    }

    @Test
    void getConstructor() {
        assertNotNull(ReflectUtil.getConstructor(String.class));
    }
}