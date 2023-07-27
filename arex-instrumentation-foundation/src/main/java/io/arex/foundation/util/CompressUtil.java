package io.arex.foundation.util;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Compress/decompress util
 *
 *
 * @date 2021/11/09
 */
public class CompressUtil {
    public static final int BYTES_BUFFER_LENGTH = 1024;
    public static final byte[] ZERO_BYTE = new byte[0];
    private static final Logger LOGGER = LoggerFactory.getLogger(CompressUtil.class);

    public static byte[] zstdCompress(String original, Charset charsetName) {
        return zstdCompress(original.getBytes(charsetName));
    }

    /**
     * zstd compress
     * @param original original string
     * @return
     */
    public static byte[] zstdCompress(byte[] original) {
        if (original == null || original.length == 0) {
            return ZERO_BYTE;
        }

        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(original);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(byteInputStream.available());
            ZstdOutputStreamNoFinalizer zstdOutputStream = new ZstdOutputStreamNoFinalizer(byteOutputStream)) {

            byte[] buffer = new byte[BYTES_BUFFER_LENGTH];
            for (int length; (length = byteInputStream.read(buffer, 0, BYTES_BUFFER_LENGTH)) != -1; ) {
                zstdOutputStream.write(buffer, 0, length);
            }

            zstdOutputStream.flush();
            zstdOutputStream.close();
            return byteOutputStream.toByteArray();
        } catch (Throwable e) {
            LOGGER.warn("[[title=arex.compress]]", e);
            return ZERO_BYTE;
        }
    }

    public static String zstdDecompress(InputStream inputStream, Charset charsetName) {
        try (ZstdInputStreamNoFinalizer zstdInputStream = new ZstdInputStreamNoFinalizer(inputStream);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(inputStream.available())) {

            byte[] buffer = new byte[BYTES_BUFFER_LENGTH];
            for (int length; (length = zstdInputStream.read(buffer, 0, BYTES_BUFFER_LENGTH)) != -1; ) {
                byteOutputStream.write(buffer, 0, length);
            }

            return byteOutputStream.toString(charsetName.name());
        } catch (Throwable e) {
            LOGGER.warn("[[title=arex.decompress]]", e);
            return null;
        }
    }

    public static String zstdDecompress(byte[] bytes, Charset charsetName) {
        return zstdDecompress(new ByteArrayInputStream(bytes), charsetName);
    }

    public static class ZstdInputStreamNoFinalizer extends ZstdInputStream {
        ZstdInputStreamNoFinalizer(InputStream inputStream) throws IOException {
            super(inputStream);
        }

        @Override
        public void finalize() { }
    }

    public static class ZstdOutputStreamNoFinalizer extends ZstdOutputStream {
        public ZstdOutputStreamNoFinalizer(OutputStream outStream) throws IOException {
            super(outStream);
        }

        @Override
        public void finalize() { }
    }
}
