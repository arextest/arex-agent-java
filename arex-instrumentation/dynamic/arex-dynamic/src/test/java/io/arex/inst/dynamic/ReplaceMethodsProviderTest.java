package io.arex.inst.dynamic;

import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.DynamicClassEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReplaceMethodsProviderTest {

    @Test
    void add() {
        final ReplaceMethodsProvider replaceMethods = new ReplaceMethodsProvider();
        replaceMethods.add(null);
        Assertions.assertEquals(0, replaceMethods.getSearchMethodMap().size());
        replaceMethods.add(new DynamicClassEntity("testClassName", "testOperation",  null,
                ArexConstants.UUID_SIGNATURE));
        Assertions.assertEquals(1, replaceMethods.getSearchMethodMap().size());
        Assertions.assertEquals("testOperation", replaceMethods.getSearchMethodMap().get(
                ArexConstants.UUID_SIGNATURE).get(0));
        replaceMethods.add(new DynamicClassEntity("testClassName", "testOperation2",  null,
                ArexConstants.UUID_SIGNATURE));
        Assertions.assertEquals(1, replaceMethods.getSearchMethodMap().size());
        Assertions.assertEquals("testOperation2", replaceMethods.getSearchMethodMap().get(
                ArexConstants.UUID_SIGNATURE).get(1));
    }

}