package io.arex.foundation.serializer;

import io.arex.agent.bootstrap.util.StringUtil;
import io.arex.foundation.serializer.ProtoTest.HelloProtoRequestType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProtoJsonSerializerTest {

    @Test
    void serialize() {
        // serialize no protobuf but not throw exception
        ProtoJsonSerializer.getInstance().serialize("test");
        assertDoesNotThrow(() -> ProtoJsonSerializer.getInstance().serialize("test"));
        // serialize protobuf
        final HelloProtoRequestType requestType = HelloProtoRequestType.newBuilder().setName("test").build();
        final String json = ProtoJsonSerializer.getInstance().serialize(requestType);
        Assertions.assertFalse(StringUtil.isEmpty(json));


        // deserialize no protobuf but not throw exception
        assertDoesNotThrow(() -> ProtoJsonSerializer.getInstance().deserialize("test", String.class));

        // deserialize empty
        final HelloProtoRequestType emptyResult = ProtoJsonSerializer.getInstance().deserialize("", HelloProtoRequestType.class);
        assertNull(emptyResult);
    }

    @Test
    void deserialize() {
    }
}
