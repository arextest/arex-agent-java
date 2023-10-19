package io.arex.foundation.serializer.custom;

import static org.junit.jupiter.api.Assertions.*;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.serializer.GsonSerializer;
import io.arex.foundation.serializer.ProtoTest.HelloProtoRequestType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProtobufAdapterFactoryTest {
    @Test
    void testProtobufAdapter() throws Throwable {
        final HelloProtoRequestType requestType = HelloProtoRequestType.newBuilder().setName("test").build();
        final String json = GsonSerializer.INSTANCE.serialize(requestType);
        Assertions.assertFalse(StringUtil.isEmpty(json));
        final HelloProtoRequestType actualResult = GsonSerializer.INSTANCE.deserialize(json, HelloProtoRequestType.class);
        assert actualResult != null;
    }

}