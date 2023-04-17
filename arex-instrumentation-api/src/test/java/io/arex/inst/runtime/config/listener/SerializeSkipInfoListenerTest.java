package io.arex.inst.runtime.config.listener;


import static org.junit.jupiter.api.Assertions.assertTrue;

import io.arex.inst.runtime.config.Config;
import io.arex.inst.runtime.serializer.Serializer.Builder;
import io.arex.inst.runtime.serializer.StringSerializable;
import java.lang.reflect.Type;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Map;

import io.arex.inst.runtime.config.ConfigBuilder;
import io.arex.inst.runtime.model.ArexConstants;
import io.arex.inst.runtime.model.SerializeSkipInfo;
import io.arex.inst.runtime.serializer.Serializer;

class SerializeSkipInfoListenerTest {
    static SerializeSkipInfoListener listener = null;

    @BeforeAll
    static void setUp(){
        listener = new SerializeSkipInfoListener();
    }

    @AfterAll
    static void tearDown() {
        listener = null;
        Mockito.clearAllCaches();
    }
    @Test
    void serializeSkipInfoListenerTest() throws Exception {
        Builder serializeBuilder = Serializer.builder(new TestSerialize());
        serializeBuilder.addSerializer("test", new TestSerialize()).build();
        SerializeSkipInfo skipInfo = new SerializeSkipInfo();
        skipInfo.setFullClassName("testClassName");
        skipInfo.setFieldName("testFieldName");
        ConfigBuilder builder = ConfigBuilder.create("test");
        builder.addProperty(ArexConstants.SERIALIZE_SKIP_INFO_CONFIG_KEY, "testSkipInfo");
        builder.build();

        assertTrue(listener.validate(Config.get()));

        // recreate serializer
        listener.load(Config.get());
        Map<String, StringSerializable> serializers = Serializer.getINSTANCE().getSerializers();
        for (Map.Entry<String, StringSerializable> entry : serializers.entrySet()) {
            StringSerializable stringSerializable = entry.getValue();
            if (stringSerializable instanceof TestSerialize) {
                TestSerialize testSerialize = (TestSerialize) stringSerializable;
                assert testSerialize.isRecreate();
            }
        }

        // not recreate serializer
        StringSerializable defaultSerializer = Serializer.getINSTANCE().getSerializer();
        listener.load(Config.get());
        Assertions.assertEquals(Serializer.getINSTANCE().getSerializer().hashCode(), defaultSerializer.hashCode());
    }

    static class TestSerialize implements StringSerializable {
        boolean recreate = false;

        public boolean isRecreate() {
            return recreate;
        }

        public void setRecreate(boolean recreate) {
            this.recreate = recreate;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public String serialize(Object object) {
            return null;
        }

        @Override
        public <T> T deserialize(String value, Class<T> clazz) {
            SerializeSkipInfo skipInfo = new SerializeSkipInfo();
            skipInfo.setFullClassName("testClassName");
            skipInfo.setFieldName("testFieldName");
            if (value.equals("testSkipInfo2")) {
                return null;
            }
            return (T) Arrays.asList(skipInfo);
        }

        @Override
        public <T> T deserialize(String value, Type type) {
            SerializeSkipInfo skipInfo = new SerializeSkipInfo();
            skipInfo.setFullClassName("testClassName");
            skipInfo.setFieldName("testFieldName");
            if (value.equals("testSkipInfo2")) {
                return null;
            }
            return (T) Arrays.asList(skipInfo);
        }

        @Override
        public StringSerializable reCreateSerializer() {
            TestSerialize newSerializer = new TestSerialize();
            newSerializer.setRecreate(true);
            return newSerializer;
        }
    }
}
