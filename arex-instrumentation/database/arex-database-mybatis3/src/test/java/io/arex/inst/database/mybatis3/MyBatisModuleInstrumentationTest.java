package io.arex.inst.database.mybatis3;

import org.junit.jupiter.api.Test;

class MyBatisModuleInstrumentationTest {

    @Test
    void instrumentationTypes() {
        new MyBatisModuleInstrumentation().instrumentationTypes();
    }
}