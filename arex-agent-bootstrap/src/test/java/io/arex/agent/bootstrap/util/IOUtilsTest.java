package io.arex.agent.bootstrap.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class IOUtilsTest {
    @Test
    void copyToByteArray() throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            builder.append(i);
        }
        InputStream inputStream = new ByteArrayInputStream(builder.toString().getBytes());
        byte[] bytes = IOUtils.copyToByteArray(inputStream);
        assertEquals(builder.toString(), new String(bytes));
    }
}
