package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CollectionUtilTest {

    @Test
    void isEmpty() {
        assertTrue(CollectionUtil.isEmpty(null));
        assertTrue(CollectionUtil.isEmpty(Collections.emptyList()));
    }

    @Test
    void isNotEmpty() {
        assertTrue(CollectionUtil.isNotEmpty(Collections.singleton("test")));
    }

    @Test
    void emptyList() {
        assertInstanceOf(ArrayList.class, CollectionUtil.emptyList());
    }

    @Test
    void newArrayList() {
        List<String> actualResult = CollectionUtil.newArrayList(null);
        assertInstanceOf(ArrayList.class, actualResult);

        actualResult = CollectionUtil.newArrayList("test");
        assertInstanceOf(ArrayList.class, actualResult);
    }
}
