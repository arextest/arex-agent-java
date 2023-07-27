package io.arex.foundation.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CompressUtilTest {

    @Test
    void test() {
        byte[] actualBytes = CompressUtil.zstdCompress("", StandardCharsets.UTF_8);
        assertArrayEquals(new byte[0], actualBytes);

        actualBytes = CompressUtil.zstdCompress("hello AREX", StandardCharsets.UTF_8);
        String expected = CompressUtil.zstdDecompress(actualBytes, StandardCharsets.UTF_8);
        assertEquals("hello AREX", expected);
    }
}
