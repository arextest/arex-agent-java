package io.arex.agent.bootstrap.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
    private IOUtils() {}
    public static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public static long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long total = 0;
        int read;
        while (EOF != (read = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, read);
            total += read;
        }
        outputStream.flush();
        return total;
    }

    public static byte[] copyToByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }
}
