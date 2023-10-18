package io.arex.inst.httpclient.apache.common;

import io.arex.agent.bootstrap.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.util.Args;

public class CachedHttpEntityWrapper extends AbstractHttpEntity {

    private final byte[] cachedBody;
    private final InputStream content;
    private final HttpEntity entity;

    public CachedHttpEntityWrapper(HttpEntity entity) throws IOException {
        this.entity = entity;
        this.cachedBody = IOUtils.copyToByteArray(entity.getContent());
        this.content = new ByteArrayInputStream(cachedBody);

    }

    @Override
    public boolean isRepeatable() {
        return this.entity.isRepeatable();
    }

    @Override
    public long getContentLength() {
        return this.entity.getContentLength();
    }

    @Override
    public InputStream getContent() throws UnsupportedOperationException {
        return new ByteArrayInputStream(this.cachedBody);
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        IOUtils.copy(this.content, outStream);
    }

    @Override
    public boolean isStreaming() {
        return this.entity.isStreaming();
    }

    public byte[] getCachedBody() {
        return this.cachedBody;
    }
}
