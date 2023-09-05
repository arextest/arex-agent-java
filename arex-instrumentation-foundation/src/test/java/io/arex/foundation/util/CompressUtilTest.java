package io.arex.foundation.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    @Test
    void testCompatible() {
        String oldCompressString = "KLUv/QBYJAgAltE3JDCLOAcQDrWkTCPhBm9L2vigBeJYVw+ES3bvwu/pHjBwMDDAASsAMQAtAAzxS6NLBfbeBg8o7tWvx5hO8bSXSw2cXV/"
                + "uI+wqUQ6KYaSdz5dz07JD7ZG7OKMoAIimCVASSbMsidMsCYNIlCVJWDQEzg4xIIaPMoNKJyl/VTpxsy+MjVN6UCG106Nxd1IQfDzC3lGnBm20Rpz91TiM4jiMg1i"
                + "cxMKurzkWtk7l50th79W3kBsk1WNzrhXoa45eZZ6ew9OOFEIasErwq3q0kOAQbbpDQnLhv8aFUqycm7ajdgwARRmAwGN29RL2nXEYEDb8ug9tmqh5KPj4mTh9ehy1EAUBAAA=";
        final byte[] oldCompressBytes = Base64.getDecoder().decode(oldCompressString);
        final String newDeCompressString = CompressUtil.zstdDecompress(oldCompressBytes, StandardCharsets.UTF_8);
        final byte[] newCompressBytes = CompressUtil.zstdCompress(newDeCompressString, StandardCharsets.UTF_8);
        String newCompressString = Base64.getEncoder().encodeToString(newCompressBytes);
        assertEquals(oldCompressString, newCompressString);
    }
}
