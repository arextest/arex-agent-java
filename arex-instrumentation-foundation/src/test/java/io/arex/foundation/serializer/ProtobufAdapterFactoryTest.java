package io.arex.foundation.serializer;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.serializer.gson.GsonSerializer;
import io.arex.foundation.serializer.ProtoTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProtobufAdapterFactoryTest {
    @Test
    void testProtobufAdapter() throws Throwable {
        final ProtoTest.HelloProtoRequestType requestType = ProtoTest.HelloProtoRequestType.newBuilder().setName("test").build();
        final String json = GsonSerializer.INSTANCE.serialize(requestType);
        Assertions.assertFalse(StringUtil.isEmpty(json));
        final ProtoTest.HelloProtoRequestType actualResult = GsonSerializer.INSTANCE.deserialize(json, ProtoTest.HelloProtoRequestType.class);
        assert actualResult != null;
    }
}
