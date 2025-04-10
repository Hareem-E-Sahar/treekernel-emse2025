package org.apache.http.impl.nio.codecs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.nio.FileContentDecoder;
import org.apache.http.nio.reactor.SessionInputBuffer;

/**
 * Content decoder that reads data without any transformation. The end of the 
 * content entity is demarcated by closing the underlying connection 
 * (EOF condition). Entities transferred using this input stream can be of 
 * unlimited length.
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
public class IdentityDecoder extends AbstractContentDecoder implements FileContentDecoder {

    public IdentityDecoder(final ReadableByteChannel channel, final SessionInputBuffer buffer, final HttpTransportMetricsImpl metrics) {
        super(channel, buffer, metrics);
    }

    /**
     * Sets the completed status of this decoder. Normally this is not necessary
     * (the decoder will automatically complete when the underlying channel
     * returns EOF). It is useful to mark the decoder as completed if you have
     * some other means to know all the necessary data has been read and want to
     * reuse the underlying connection for more messages.
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int read(final ByteBuffer dst) throws IOException {
        if (dst == null) {
            throw new IllegalArgumentException("Byte buffer may not be null");
        }
        if (this.completed) {
            return -1;
        }
        int bytesRead;
        if (this.buffer.hasData()) {
            bytesRead = this.buffer.read(dst);
        } else {
            bytesRead = this.channel.read(dst);
            if (bytesRead > 0) {
                this.metrics.incrementBytesTransferred(bytesRead);
            }
        }
        if (bytesRead == -1) {
            this.completed = true;
        }
        return bytesRead;
    }

    public long transfer(final FileChannel dst, long position, long count) throws IOException {
        if (dst == null) {
            return 0;
        }
        if (this.completed) {
            return 0;
        }
        long bytesRead;
        if (this.buffer.hasData()) {
            dst.position(position);
            bytesRead = this.buffer.read(dst);
        } else {
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
        }
        return bytesRead;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[identity; completed: ");
        buffer.append(this.completed);
        buffer.append("]");
        return buffer.toString();
    }
}
