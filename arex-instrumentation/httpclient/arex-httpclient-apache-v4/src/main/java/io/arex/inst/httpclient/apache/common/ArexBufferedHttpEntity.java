package io.arex.inst.httpclient.apache.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/**
 * This class only buffer the original HttpEntity content into a byte array, tt does not modify any
 * behavior of the original HttpEntity.
 * This class is known to have performance issues comparing to the original BufferedHttpEntity, but
 * it provides consistent behavior with the original HttpEntity.
 * @see org.apache.http.entity.BufferedHttpEntity
 * @author: QizhengMo
 * @date: 2025/3/12 15:35
 */
public class ArexBufferedHttpEntity extends HttpEntityWrapper {
  private final byte[] buffer;

  public ArexBufferedHttpEntity(HttpEntity wrappedEntity) throws IOException {
    super(wrappedEntity);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();

    // This class is only used in Arex Agent, so we are almost always the first to consume the content.
    wrappedEntity.writeTo(out);
    out.flush();
    this.buffer = out.toByteArray();
  }

  /**
   * Return a copy of the original content of the wrapped HttpEntity.
   */
  @Override
  public InputStream getContent() throws IOException {
    return new ByteArrayInputStream(buffer);
  }

  @Override
  public void writeTo(final OutputStream outStream) throws IOException {
    if (this.buffer != null) {
      outStream.write(this.buffer);
    } else {
      super.writeTo(outStream);
    }
  }
}
