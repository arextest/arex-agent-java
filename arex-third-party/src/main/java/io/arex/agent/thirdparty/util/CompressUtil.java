package io.arex.agent.thirdparty.util;

import com.github.luben.zstd.RecyclingBufferPool;
import com.github.luben.zstd.ZstdInputStreamNoFinalizer;
import com.github.luben.zstd.ZstdOutputStreamNoFinalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

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
            ZstdOutputStreamNoFinalizer zstdOutputStream = new ZstdOutputStreamNoFinalizer(byteOutputStream,
                    RecyclingBufferPool.INSTANCE)) {

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
        try (ZstdInputStreamNoFinalizer zstdInputStream = new ZstdInputStreamNoFinalizer(inputStream,
                RecyclingBufferPool.INSTANCE);
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

    public static String zstdDecompress(byte[] bytes) {
        return zstdDecompress(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
//        String original = "hello world";
//        byte[] compressed = zstdCompress(original, StandardCharsets.UTF_8);
//        System.out.println("compressed: " + new String(compressed, StandardCharsets.UTF_8));

        String compressStr = "KLUv/QBY7QcAVlA0IkBpnQPJ3kIW0P5pC12ZhU4ZJcQ2QFhQmFrViUQV4qooalwpACsALAAKKTwIvkn5ic95ruc+CgkePN1zIzVoSZmhHZefK3FP5z0bCHenHJh4I4cGgCAH0w4lacnrjopYhaRPo61OgHsuwyY+V1vYgrjnNo8Xz3VwT4eA4RQLqr+qhnvcZiQed+vXE6qngVkWaSNe5dBYSxjeCSKe+AYMq3omDMKUxSmKC9Uj2CxDugWM6tfSso7brKVVKFs87oZFsDk6PgSlzAIMes1gOunC3TcQAFgOsQXAyYAsgLsApMBmBZIAnMC4UgY6UHeTkYEKGUDmaJ534xYTCg==";
        byte[] compressBytes = Base64.getDecoder().decode(compressStr);
        String decompressed = zstdDecompress(compressBytes);
//        String decompressed = zstdDecompress(compressStr.getBytes(StandardCharsets.ISO_8859_1));
        System.out.println("decompressed: " + decompressed);
    }
}
