package io.arex.inst.servlet.v3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * FastByteArrayOutputStream
 *
 *
 * @date 2022/03/03
 */
public class FastByteArrayOutputStream extends OutputStream {
    private static final int DEFAULT_BLOCK_SIZE = 256;
    private final Deque<byte[]> buffers;
    private final int initialBlockSize;
    private int nextBlockSize;
    private int alreadyBufferedSize;
    private int index;
    private boolean closed;

    public FastByteArrayOutputStream() {
        this(256);
    }

    public FastByteArrayOutputStream(int initialBlockSize) {
        this.buffers = new ArrayDeque();
        this.nextBlockSize = 0;
        this.alreadyBufferedSize = 0;
        this.index = 0;
        this.closed = false;
        if (!(initialBlockSize > 0)) {
            throw new IllegalArgumentException("Initial block size must be greater than 0");
        }
        this.initialBlockSize = initialBlockSize;
        this.nextBlockSize = initialBlockSize;
    }

    public void write(int datum) throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        } else {
            if (this.buffers.peekLast() == null || ((byte[])this.buffers.getLast()).length == this.index) {
                this.addBuffer(1);
            }

            ((byte[])this.buffers.getLast())[this.index++] = (byte)datum;
        }
    }

    public void write(byte[] data, int offset, int length) throws IOException {
        if (offset >= 0 && offset + length <= data.length && length >= 0) {
            if (this.closed) {
                throw new IOException("Stream closed");
            } else {
                if (this.buffers.peekLast() == null || ((byte[])this.buffers.getLast()).length == this.index) {
                    this.addBuffer(length);
                }

                if (this.index + length > ((byte[])this.buffers.getLast()).length) {
                    int pos = offset;

                    do {
                        if (this.index == ((byte[])this.buffers.getLast()).length) {
                            this.addBuffer(length);
                        }

                        int copyLength = ((byte[])this.buffers.getLast()).length - this.index;
                        if (length < copyLength) {
                            copyLength = length;
                        }

                        System.arraycopy(data, pos, this.buffers.getLast(), this.index, copyLength);
                        pos += copyLength;
                        this.index += copyLength;
                        length -= copyLength;
                    } while(length > 0);
                } else {
                    System.arraycopy(data, offset, this.buffers.getLast(), this.index, length);
                    this.index += length;
                }

            }
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public void close() {
        this.closed = true;
    }

    public String toString() {
        return new String(this.toByteArrayUnsafe());
    }

    public int size() {
        return this.alreadyBufferedSize + this.index;
    }

    public byte[] toByteArrayUnsafe() {
        int totalSize = this.size();
        if (totalSize == 0) {
            return new byte[0];
        } else {
            this.resize(totalSize);
            return (byte[])this.buffers.getFirst();
        }
    }

    public byte[] toByteArray() {
        byte[] bytesUnsafe = this.toByteArrayUnsafe();
        return (byte[])bytesUnsafe.clone();
    }

    public void reset() {
        this.buffers.clear();
        this.nextBlockSize = this.initialBlockSize;
        this.closed = false;
        this.index = 0;
        this.alreadyBufferedSize = 0;
    }

    public InputStream getInputStream() {
        return new FastByteArrayOutputStream.FastByteArrayInputStream(this);
    }

    public void writeTo(OutputStream out) throws IOException {
        Iterator it = this.buffers.iterator();

        while(it.hasNext()) {
            byte[] bytes = (byte[])it.next();
            if (it.hasNext()) {
                out.write(bytes, 0, bytes.length);
            } else {
                out.write(bytes, 0, this.index);
            }
        }

    }

    public void resize(int targetCapacity) {
        if (!(targetCapacity >= this.size())) {
            throw new IllegalArgumentException("New capacity must not be smaller than current size");
        }
        if (this.buffers.peekFirst() == null) {
            this.nextBlockSize = targetCapacity - this.size();
        } else if (this.size() != targetCapacity || ((byte[])this.buffers.getFirst()).length != targetCapacity) {
            int totalSize = this.size();
            byte[] data = new byte[targetCapacity];
            int pos = 0;
            Iterator it = this.buffers.iterator();

            while(it.hasNext()) {
                byte[] bytes = (byte[])it.next();
                if (it.hasNext()) {
                    System.arraycopy(bytes, 0, data, pos, bytes.length);
                    pos += bytes.length;
                } else {
                    System.arraycopy(bytes, 0, data, pos, this.index);
                }
            }

            this.buffers.clear();
            this.buffers.add(data);
            this.index = totalSize;
            this.alreadyBufferedSize = 0;
        }

    }

    private void addBuffer(int minCapacity) {
        if (this.buffers.peekLast() != null) {
            this.alreadyBufferedSize += this.index;
            this.index = 0;
        }

        if (this.nextBlockSize < minCapacity) {
            this.nextBlockSize = nextPowerOf2(minCapacity);
        }

        this.buffers.add(new byte[this.nextBlockSize]);
        this.nextBlockSize *= 2;
    }

    private static int nextPowerOf2(int val) {
        --val;
        val |= val >> 1;
        val |= val >> 2;
        val |= val >> 4;
        val |= val >> 8;
        val |= val >> 16;
        ++val;
        return val;
    }

    private static final class FastByteArrayInputStream extends UpdateMessageDigestInputStream {
        private final FastByteArrayOutputStream fastByteArrayOutputStream;
        private final Iterator<byte[]> buffersIterator;
        private byte[] currentBuffer;
        private int currentBufferLength = 0;
        private int nextIndexInCurrentBuffer = 0;
        private int totalBytesRead = 0;

        public FastByteArrayInputStream(FastByteArrayOutputStream fastByteArrayOutputStream) {
            this.fastByteArrayOutputStream = fastByteArrayOutputStream;
            this.buffersIterator = fastByteArrayOutputStream.buffers.iterator();
            if (this.buffersIterator.hasNext()) {
                this.currentBuffer = (byte[])this.buffersIterator.next();
                if (this.currentBuffer == fastByteArrayOutputStream.buffers.getLast()) {
                    this.currentBufferLength = fastByteArrayOutputStream.index;
                } else {
                    this.currentBufferLength = this.currentBuffer != null ? this.currentBuffer.length : 0;
                }
            }

        }

        public int read() {
            if (this.currentBuffer == null) {
                return -1;
            } else if (this.nextIndexInCurrentBuffer < this.currentBufferLength) {
                ++this.totalBytesRead;
                return this.currentBuffer[this.nextIndexInCurrentBuffer++] & 255;
            } else {
                if (this.buffersIterator.hasNext()) {
                    this.currentBuffer = (byte[])this.buffersIterator.next();
                    this.updateCurrentBufferLength();
                    this.nextIndexInCurrentBuffer = 0;
                } else {
                    this.currentBuffer = null;
                }

                return this.read();
            }
        }

        public int read(byte[] b) {
            return this.read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) {
            if (off >= 0 && len >= 0 && len <= b.length - off) {
                if (len == 0) {
                    return 0;
                } else if (this.currentBuffer == null) {
                    return -1;
                } else if (this.nextIndexInCurrentBuffer < this.currentBufferLength) {
                    int bytesToCopy = Math.min(len, this.currentBufferLength - this.nextIndexInCurrentBuffer);
                    System.arraycopy(this.currentBuffer, this.nextIndexInCurrentBuffer, b, off, bytesToCopy);
                    this.totalBytesRead += bytesToCopy;
                    this.nextIndexInCurrentBuffer += bytesToCopy;
                    int remaining = this.read(b, off + bytesToCopy, len - bytesToCopy);
                    return bytesToCopy + Math.max(remaining, 0);
                } else {
                    if (this.buffersIterator.hasNext()) {
                        this.currentBuffer = (byte[])this.buffersIterator.next();
                        this.updateCurrentBufferLength();
                        this.nextIndexInCurrentBuffer = 0;
                    } else {
                        this.currentBuffer = null;
                    }

                    return this.read(b, off, len);
                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public long skip(long n) throws IOException {
            if (n > 2147483647L) {
                throw new IllegalArgumentException("n exceeds maximum (2147483647): " + n);
            } else if (n == 0L) {
                return 0L;
            } else if (n < 0L) {
                throw new IllegalArgumentException("n must be 0 or greater: " + n);
            } else {
                int len = (int)n;
                if (this.currentBuffer == null) {
                    return 0L;
                } else if (this.nextIndexInCurrentBuffer < this.currentBufferLength) {
                    int bytesToSkip = Math.min(len, this.currentBufferLength - this.nextIndexInCurrentBuffer);
                    this.totalBytesRead += bytesToSkip;
                    this.nextIndexInCurrentBuffer += bytesToSkip;
                    return (long)bytesToSkip + this.skip((long)(len - bytesToSkip));
                } else {
                    if (this.buffersIterator.hasNext()) {
                        this.currentBuffer = (byte[])this.buffersIterator.next();
                        this.updateCurrentBufferLength();
                        this.nextIndexInCurrentBuffer = 0;
                    } else {
                        this.currentBuffer = null;
                    }

                    return this.skip((long)len);
                }
            }
        }

        public int available() {
            return this.fastByteArrayOutputStream.size() - this.totalBytesRead;
        }

        public void updateMessageDigest(MessageDigest messageDigest) {
            this.updateMessageDigest(messageDigest, this.available());
        }

        public void updateMessageDigest(MessageDigest messageDigest, int len) {
            if (this.currentBuffer != null) {
                if (len != 0) {
                    if (len < 0) {
                        throw new IllegalArgumentException("len must be 0 or greater: " + len);
                    } else {
                        if (this.nextIndexInCurrentBuffer < this.currentBufferLength) {
                            int bytesToCopy = Math.min(len, this.currentBufferLength - this.nextIndexInCurrentBuffer);
                            messageDigest.update(this.currentBuffer, this.nextIndexInCurrentBuffer, bytesToCopy);
                            this.nextIndexInCurrentBuffer += bytesToCopy;
                            this.updateMessageDigest(messageDigest, len - bytesToCopy);
                        } else {
                            if (this.buffersIterator.hasNext()) {
                                this.currentBuffer = (byte[])this.buffersIterator.next();
                                this.updateCurrentBufferLength();
                                this.nextIndexInCurrentBuffer = 0;
                            } else {
                                this.currentBuffer = null;
                            }

                            this.updateMessageDigest(messageDigest, len);
                        }

                    }
                }
            }
        }

        private void updateCurrentBufferLength() {
            if (this.currentBuffer == this.fastByteArrayOutputStream.buffers.getLast()) {
                this.currentBufferLength = this.fastByteArrayOutputStream.index;
            } else {
                this.currentBufferLength = this.currentBuffer != null ? this.currentBuffer.length : 0;
            }

        }
    }

    static abstract class UpdateMessageDigestInputStream extends InputStream {
        UpdateMessageDigestInputStream() {
        }

        public void updateMessageDigest(MessageDigest messageDigest) throws IOException {
            int data;
            while((data = this.read()) != -1) {
                messageDigest.update((byte)data);
            }

        }

        public void updateMessageDigest(MessageDigest messageDigest, int len) throws IOException {
            int data;
            for(int bytesRead = 0; bytesRead < len && (data = this.read()) != -1; ++bytesRead) {
                messageDigest.update((byte)data);
            }

        }
    }
}
