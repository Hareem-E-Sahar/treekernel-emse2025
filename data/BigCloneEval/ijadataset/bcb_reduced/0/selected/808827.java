package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.nio.FileContentDecoder;
import org.apache.http.nio.reactor.SessionInputBuffer;

/**
 * Content decoder that cuts off after a defined number of bytes. This class 
 * is used to receive content of HTTP messages where the end of the content 
 * entity is determined by the value of the <code>Content-Length header</code>. 
 * Entities transferred using this stream can be maximum {@link Long#MAX_VALUE}
 * long. 
 * <p>
 * This decoder is optimized to transfer data directly from the underlying 
 * I/O session's channel to a {@link FileChannel}, whenever 
 * possible avoiding intermediate buffering in the session buffer. 
 *
 *
 * @version $Revision: 744538 $
 * 
 * @since 4.0
 */
public class LengthDelimitedDecoder extends AbstractContentDecoder implements FileContentDecoder {

    private final long contentLength;

    private long len;

    public LengthDelimitedDecoder(final ReadableByteChannel channel, final SessionInputBuffer buffer, final HttpTransportMetricsImpl metrics, long contentLength) {
        super(channel, buffer, metrics);
        if (contentLength < 0) {
            throw new IllegalArgumentException("Content length may not be negative");
        }
        this.contentLength = contentLength;
    }

    public int read(final ByteBuffer dst) throws IOException {
        if (dst == null) {
            throw new IllegalArgumentException("Byte buffer may not be null");
        }
        if (this.completed) {
            return -1;
        }
        int lenRemaining = (int) (this.contentLength - this.len);
        int bytesRead;
        if (this.buffer.hasData()) {
            int maxLen = Math.min(lenRemaining, this.buffer.length());
            bytesRead = this.buffer.read(dst, maxLen);
        } else {
            if (dst.remaining() > lenRemaining) {
                int oldLimit = dst.limit();
                int newLimit = oldLimit - (dst.remaining() - lenRemaining);
                dst.limit(newLimit);
                bytesRead = this.channel.read(dst);
                dst.limit(oldLimit);
            } else {
                bytesRead = this.channel.read(dst);
            }
            if (bytesRead > 0) {
                this.metrics.incrementBytesTransferred(bytesRead);
            }
        }
        if (bytesRead == -1) {
            this.completed = true;
            return -1;
        }
        this.len += bytesRead;
        if (this.len >= this.contentLength) {
            this.completed = true;
        }
        return bytesRead;
    }

    public long transfer(final FileChannel dst, long position, long count) throws IOException {
        if (dst == null) {
            return 0;
        }
        if (this.completed) {
            return -1;
        }
        int lenRemaining = (int) (this.contentLength - this.len);
        long bytesRead;
        if (this.buffer.hasData()) {
            int maxLen = Math.min(lenRemaining, this.buffer.length());
            dst.position(position);
            bytesRead = this.buffer.read(dst, maxLen);
        } else {
            if (count > lenRemaining) {
                count = lenRemaining;
            }
            if (this.channel.isOpen()) {
                if (dst.size() < position) throw new IOException("FileChannel.size() [" + dst.size() + "] < position [" + position + "].  Please grow the file before writing.");
                bytesRead = dst.transferFrom(this.channel, position, count);
            } else {
                bytesRead = -1;
            }
            if (bytesRead > 0) {
                this.metrics.incrementBytesTransferred(bytesRead);
            }
        }
        if (bytesRead == -1) {
            this.completed = true;
            return -1;
        }
        this.len += bytesRead;
        if (this.len >= this.contentLength) {
            this.completed = true;
        }
        return bytesRead;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[content length: ");
        buffer.append(this.contentLength);
        buffer.append("; pos: ");
        buffer.append(this.len);
        buffer.append("; completed: ");
        buffer.append(this.completed);
        buffer.append("]");
        return buffer.toString();
    }
}
