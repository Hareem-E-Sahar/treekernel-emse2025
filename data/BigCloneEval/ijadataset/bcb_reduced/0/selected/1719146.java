package org.jboss.netty.handler.codec.http2;

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Memory implementation of Attributes
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author <a href="http://openr66.free.fr/">Frederic Bregier</a>
 *
 */
public class MemoryAttribute extends AbstractMemoryHttpData implements Attribute {

    public MemoryAttribute(String name) {
        super(name, HttpCodecUtil.DEFAULT_CHARSET, 0);
    }

    /**
     *
     * @param name
     * @param value
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public MemoryAttribute(String name, String value) throws NullPointerException, IllegalArgumentException, IOException {
        super(name, HttpCodecUtil.DEFAULT_CHARSET, 0);
        setValue(value);
    }

    public HttpDataType getHttpDataType() {
        return HttpDataType.Attribute;
    }

    public String getValue() {
        return getChannelBuffer().toString(charset);
    }

    public void setValue(String value) throws IOException {
        if (value == null) {
            throw new NullPointerException("value");
        }
        byte[] bytes = value.getBytes(charset);
        ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(bytes);
        if (definedSize > 0) {
            definedSize = buffer.readableBytes();
        }
        setContent(buffer);
    }

    @Override
    public void addContent(ChannelBuffer buffer, boolean last) throws IOException {
        int localsize = buffer.readableBytes();
        if (definedSize > 0 && definedSize < size + localsize) {
            definedSize = size + localsize;
        }
        super.addContent(buffer, last);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Attribute)) {
            return false;
        }
        Attribute attribute = (Attribute) o;
        return getName().equalsIgnoreCase(attribute.getName());
    }

    public int compareTo(InterfaceHttpData arg0) {
        if (!(arg0 instanceof Attribute)) {
            throw new ClassCastException("Cannot compare " + getHttpDataType() + " with " + arg0.getHttpDataType());
        }
        return compareTo((Attribute) arg0);
    }

    public int compareTo(Attribute o) {
        return getName().compareToIgnoreCase(o.getName());
    }

    @Override
    public String toString() {
        return getName() + "=" + getValue();
    }
}
