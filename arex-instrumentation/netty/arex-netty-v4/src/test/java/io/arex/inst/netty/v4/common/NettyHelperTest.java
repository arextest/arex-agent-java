package io.arex.inst.netty.v4.common;

import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NettyHelperTest {

    @Test
    void parseHeaders() {
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.set("key", "value");
        assertTrue(NettyHelper.parseHeaders(httpHeaders).size() > 0);
    }

    @Test
    void parseBody() {
        assertNull(NettyHelper.parseBody(new EmptyByteBuf(new UnpooledByteBufAllocator(false))));
        assertNotNull(NettyHelper.parseBody(UnpooledByteBufAllocator.DEFAULT.buffer().writeBytes("mock".getBytes())));
    }
}