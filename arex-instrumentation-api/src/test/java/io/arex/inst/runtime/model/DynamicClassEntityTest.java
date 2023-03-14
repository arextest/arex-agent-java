package io.arex.inst.runtime.model;

import io.arex.agent.bootstrap.util.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DynamicClassEntityTest {

    @Test
    void getKeyFormula() {
        DynamicClassEntity entity1 = new DynamicClassEntity(null, null, null, "$1.get()");
        DynamicClassEntity entity2 = new DynamicClassEntity(null, null, null, "$1 + $2.get()");
        DynamicClassEntity entity3 = new DynamicClassEntity(null, null, null, "String.valueOf($1)");
        DynamicClassEntity entity4 = new DynamicClassEntity(null, null, null, "this.clusterName");
        DynamicClassEntity entity5 = new DynamicClassEntity(null, null, null, "$_.get()");
        DynamicClassEntity entity6 = new DynamicClassEntity(null, null, null, "");

        Assertions.assertEquals(StringUtil.EMPTY, entity1.getAdditionalSignature());
        Assertions.assertEquals(StringUtil.EMPTY, entity2.getAdditionalSignature());
        Assertions.assertEquals(StringUtil.EMPTY, entity3.getAdditionalSignature());
        Assertions.assertEquals(StringUtil.EMPTY, entity4.getAdditionalSignature());
        Assertions.assertEquals(StringUtil.EMPTY, entity5.getAdditionalSignature());
        Assertions.assertEquals(StringUtil.EMPTY, entity6.getAdditionalSignature());

        DynamicClassEntity entity7 = new DynamicClassEntity(null, null, null, "java.lang.String");
        DynamicClassEntity entity8 = new DynamicClassEntity(null, null, null, ArexConstants.UUID_SIGNATURE);
        DynamicClassEntity entity9 = new DynamicClassEntity(null, null, null, ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE);

        Assertions.assertEquals(StringUtil.EMPTY, entity7.getAdditionalSignature());
        Assertions.assertEquals(ArexConstants.UUID_SIGNATURE, entity8.getAdditionalSignature());
        Assertions.assertEquals(ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE, entity9.getAdditionalSignature());

    }

    @Test
    void getGenericReturnType() {
        DynamicClassEntity entity1 = new DynamicClassEntity(null, null, null, "$1.get()");
        DynamicClassEntity entity2 = new DynamicClassEntity(null, null, null, "$1 + $2.get()");
        DynamicClassEntity entity3 = new DynamicClassEntity(null, null, null, "String.valueOf($1)");
        DynamicClassEntity entity4 = new DynamicClassEntity(null, null, null, "this.clusterName");
        DynamicClassEntity entity5 = new DynamicClassEntity(null, null, null, "$_.get()");
        DynamicClassEntity entity6 = new DynamicClassEntity(null, null, null, "");

        Assertions.assertNull(entity1.getGenericReturnType());
        Assertions.assertNull(entity2.getGenericReturnType());
        Assertions.assertNull(entity3.getGenericReturnType());
        Assertions.assertNull(entity4.getGenericReturnType());
        Assertions.assertNull(entity5.getGenericReturnType());
        Assertions.assertNull(entity6.getGenericReturnType());

        DynamicClassEntity entity7 = new DynamicClassEntity(null, null, null, "java.lang.String");
        DynamicClassEntity entity8 = new DynamicClassEntity(null, null, null, ArexConstants.UUID_SIGNATURE);
        DynamicClassEntity entity9 = new DynamicClassEntity(null, null, null, ArexConstants.CURRENT_TIME_MILLIS_SIGNATURE);
        DynamicClassEntity entity10 = new DynamicClassEntity(null, null, null, "T:java.lang.String");

        Assertions.assertNull(entity7.getGenericReturnType());
        Assertions.assertNull(entity8.getGenericReturnType());
        Assertions.assertNull(entity9.getGenericReturnType());
        Assertions.assertEquals("java.lang.String", entity10.getGenericReturnType());
    }
}