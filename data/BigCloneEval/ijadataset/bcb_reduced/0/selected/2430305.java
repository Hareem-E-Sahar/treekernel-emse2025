package org.jwebsocket.netty.http;

import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

/**
 * Decodes {@link ChannelBuffer}s into {@link HttpMessage}s and
 * {@link HttpChunk}s.
 *
 * <h3>Parameters that prevents excessive memory consumption</h3>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>The maximum length of the initial line
 *     (e.g. {@code "GET / HTTP/1.0"} or {@code "HTTP/1.0 200 OK"})
 *     If the length of the initial line exceeds this value, a
 *     {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>The maximum length of all headers.  If the sum of the length of each
 *     header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxChunkSize}</td>
 * <td>The maximum length of the content or each chunk.  If the content length
 *     (or the length of each chunk) exceeds this value, the content or chunk
 *     will be split into multiple {@link HttpChunk}s whose length is
 *     {@code maxChunkSize} at maximum.</td>
 * </tr>
 * </table>
 *
 * <h3>Chunked Content</h3>
 *
 * If the content of an HTTP message is greater than {@code maxChunkSize} or
 * the transfer encoding of the HTTP message is 'chunked', this decoder
 * generates one {@link HttpMessage} instance and its following
 * {@link HttpChunk}s per single HTTP message to avoid excessive memory
 * consumption. For example, the following HTTP message:
 * <pre>
 * GET / HTTP/1.1
 * Transfer-Encoding: chunked
 *
 * 1a
 * abcdefghijklmnopqrstuvwxyz
 * 10
 * 1234567890abcdef
 * 0
 * Content-MD5: ...
 * <i>[blank line]</i>
 * </pre>
 * triggers {@link HttpRequestDecoder} to generate 4 objects:
 * <ol>
 * <li>An {@link HttpRequest} whose {@link HttpMessage#isChunked() chunked}
 *     property is {@code true},</li>
 * <li>The first {@link HttpChunk} whose content is {@code 'abcdefghijklmnopqrstuvwxyz'},</li>
 * <li>The second {@link HttpChunk} whose content is {@code '1234567890abcdef'}, and</li>
 * <li>An {@link HttpChunkTrailer} which marks the end of the content.</li>
 * </ol>
 *
 * If you prefer not to handle {@link HttpChunk}s by yourself for your
 * convenience, insert {@link HttpChunkAggregator} after this decoder in the
 * {@link ChannelPipeline}.  However, please note that your server might not
 * be as memory efficient as without the aggregator.
 *
 * <h3>Extensibility</h3>
 *
 * Please note that this decoder is designed to be extended to implement
 * a protocol derived from HTTP, such as
 * <a href="http://en.wikipedia.org/wiki/Real_Time_Streaming_Protocol">RTSP</a> and
 * <a href="http://en.wikipedia.org/wiki/Internet_Content_Adaptation_Protocol">ICAP</a>.
 * To implement the decoder of such a derived protocol, extend this class and
 * implement all abstract methods properly.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2287 $, $Date: 2010-06-23 05:35:00 +0200 (Wed, 23 Jun 2010) $
 *
 * @apiviz.landmark
 */
public abstract class HttpMessageDecoder extends ReplayingDecoder<HttpMessageDecoder.State> {

    private final int maxInitialLineLength;

    private final int maxHeaderSize;

    private final int maxChunkSize;

    private HttpMessage message;

    private ChannelBuffer content;

    private long chunkSize;

    private int headerSize;

    /**
     * The internal state of {@link HttpMessageDecoder}.
     * <em>Internal use only</em>.
     *
     * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
     * @author <a href="http://gleamynode.net/">Trustin Lee</a>
     * @version $Rev: 2287 $, $Date: 2010-06-23 05:35:00 +0200 (Wed, 23 Jun 2010) $
     *
     * @apiviz.exclude
     */
    protected static enum State {

        SKIP_CONTROL_CHARS, READ_INITIAL, READ_HEADER, READ_VARIABLE_LENGTH_CONTENT, READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS, READ_FIXED_LENGTH_CONTENT, READ_FIXED_LENGTH_CONTENT_AS_CHUNKS, READ_CHUNK_SIZE, READ_CHUNKED_CONTENT, READ_CHUNKED_CONTENT_AS_CHUNKS, READ_CHUNK_DELIMITER, READ_CHUNK_FOOTER
    }

    /**
     * Creates a new instance with the default
     * {@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}, and
     * {@code maxChunkSize (8192)}.
     */
    protected HttpMessageDecoder() {
        this(4096, 8192, 8192);
    }

    /**
     * Creates a new instance with the specified parameters.
     */
    protected HttpMessageDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        super(State.SKIP_CONTROL_CHARS, true);
        if (maxInitialLineLength <= 0) {
            throw new IllegalArgumentException("maxInitialLineLength must be a positive integer: " + maxInitialLineLength);
        }
        if (maxHeaderSize <= 0) {
            throw new IllegalArgumentException("maxHeaderSize must be a positive integer: " + maxChunkSize);
        }
        if (maxChunkSize < 0) {
            throw new IllegalArgumentException("maxChunkSize must be a positive integer: " + maxChunkSize);
        }
        this.maxInitialLineLength = maxInitialLineLength;
        this.maxHeaderSize = maxHeaderSize;
        this.maxChunkSize = maxChunkSize;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, State state) throws Exception {
        switch(state) {
            case SKIP_CONTROL_CHARS:
                {
                    try {
                        skipControlCharacters(buffer);
                        checkpoint(State.READ_INITIAL);
                    } finally {
                        checkpoint();
                    }
                }
            case READ_INITIAL:
                {
                    String[] initialLine = splitInitialLine(readLine(buffer, maxInitialLineLength));
                    if (initialLine.length < 3) {
                        checkpoint(State.SKIP_CONTROL_CHARS);
                        return null;
                    }
                    message = createMessage(initialLine);
                    checkpoint(State.READ_HEADER);
                }
            case READ_HEADER:
                {
                    State nextState = readHeaders(buffer);
                    checkpoint(nextState);
                    if (nextState == State.READ_CHUNK_SIZE) {
                        message.setChunked(true);
                        return message;
                    } else if (nextState == State.SKIP_CONTROL_CHARS) {
                        message.removeHeader(HttpHeaders.Names.TRANSFER_ENCODING);
                        return message;
                    } else {
                        long contentLength = HttpHeaders.getContentLength(message, -1);
                        if (contentLength == 0 || contentLength == -1 && isDecodingRequest()) {
                            content = ChannelBuffers.EMPTY_BUFFER;
                            return reset();
                        }
                        switch(nextState) {
                            case READ_FIXED_LENGTH_CONTENT:
                                if (contentLength > maxChunkSize) {
                                    checkpoint(State.READ_FIXED_LENGTH_CONTENT_AS_CHUNKS);
                                    message.setChunked(true);
                                    chunkSize = HttpHeaders.getContentLength(message, -1);
                                    return message;
                                }
                                break;
                            case READ_VARIABLE_LENGTH_CONTENT:
                                if (buffer.readableBytes() > maxChunkSize) {
                                    checkpoint(State.READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS);
                                    message.setChunked(true);
                                    return message;
                                }
                                break;
                        }
                    }
                    return null;
                }
            case READ_VARIABLE_LENGTH_CONTENT:
                {
                    if (content == null) {
                        content = ChannelBuffers.dynamicBuffer(channel.getConfig().getBufferFactory());
                    }
                    content.writeBytes(buffer.readBytes(buffer.readableBytes()));
                    return reset();
                }
            case READ_VARIABLE_LENGTH_CONTENT_AS_CHUNKS:
                {
                    int chunkSize = Math.min(maxChunkSize, buffer.readableBytes());
                    HttpChunk chunk = new DefaultHttpChunk(buffer.readBytes(chunkSize));
                    if (!buffer.readable()) {
                        reset();
                        if (!chunk.isLast()) {
                            return new Object[] { chunk, HttpChunk.LAST_CHUNK };
                        }
                    }
                    return chunk;
                }
            case READ_FIXED_LENGTH_CONTENT:
                {
                    readFixedLengthContent(buffer);
                    return reset();
                }
            case READ_FIXED_LENGTH_CONTENT_AS_CHUNKS:
                {
                    long chunkSize = this.chunkSize;
                    HttpChunk chunk;
                    if (chunkSize > maxChunkSize) {
                        chunk = new DefaultHttpChunk(buffer.readBytes(maxChunkSize));
                        chunkSize -= maxChunkSize;
                    } else {
                        assert chunkSize <= Integer.MAX_VALUE;
                        chunk = new DefaultHttpChunk(buffer.readBytes((int) chunkSize));
                        chunkSize = 0;
                    }
                    this.chunkSize = chunkSize;
                    if (chunkSize == 0) {
                        reset();
                        if (!chunk.isLast()) {
                            return new Object[] { chunk, HttpChunk.LAST_CHUNK };
                        }
                    }
                    return chunk;
                }
            case READ_CHUNK_SIZE:
                {
                    String line = readLine(buffer, maxInitialLineLength);
                    int chunkSize = getChunkSize(line);
                    this.chunkSize = chunkSize;
                    if (chunkSize == 0) {
                        checkpoint(State.READ_CHUNK_FOOTER);
                        return null;
                    } else if (chunkSize > maxChunkSize) {
                        checkpoint(State.READ_CHUNKED_CONTENT_AS_CHUNKS);
                    } else {
                        checkpoint(State.READ_CHUNKED_CONTENT);
                    }
                }
            case READ_CHUNKED_CONTENT:
                {
                    assert chunkSize <= Integer.MAX_VALUE;
                    HttpChunk chunk = new DefaultHttpChunk(buffer.readBytes((int) chunkSize));
                    checkpoint(State.READ_CHUNK_DELIMITER);
                    return chunk;
                }
            case READ_CHUNKED_CONTENT_AS_CHUNKS:
                {
                    long chunkSize = this.chunkSize;
                    HttpChunk chunk;
                    if (chunkSize > maxChunkSize) {
                        chunk = new DefaultHttpChunk(buffer.readBytes(maxChunkSize));
                        chunkSize -= maxChunkSize;
                    } else {
                        assert chunkSize <= Integer.MAX_VALUE;
                        chunk = new DefaultHttpChunk(buffer.readBytes((int) chunkSize));
                        chunkSize = 0;
                    }
                    this.chunkSize = chunkSize;
                    if (chunkSize == 0) {
                        checkpoint(State.READ_CHUNK_DELIMITER);
                    }
                    if (!chunk.isLast()) {
                        return chunk;
                    }
                }
            case READ_CHUNK_DELIMITER:
                {
                    for (; ; ) {
                        byte next = buffer.readByte();
                        if (next == HttpCodecUtil.CR) {
                            if (buffer.readByte() == HttpCodecUtil.LF) {
                                checkpoint(State.READ_CHUNK_SIZE);
                                return null;
                            }
                        } else if (next == HttpCodecUtil.LF) {
                            checkpoint(State.READ_CHUNK_SIZE);
                            return null;
                        }
                    }
                }
            case READ_CHUNK_FOOTER:
                {
                    HttpChunkTrailer trailer = readTrailingHeaders(buffer);
                    if (maxChunkSize == 0) {
                        return reset();
                    } else {
                        reset();
                        return trailer;
                    }
                }
            default:
                {
                    throw new Error("Shouldn't reach here.");
                }
        }
    }

    protected boolean isContentAlwaysEmpty(HttpMessage msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse res = (HttpResponse) msg;
            int code = res.getStatus().getCode();
            if (code < 200) {
                return true;
            }
            switch(code) {
                case 204:
                case 205:
                case 304:
                    return true;
            }
        }
        return false;
    }

    private Object reset() {
        HttpMessage message = this.message;
        ChannelBuffer content = this.content;
        if (content != null) {
            message.setContent(content);
            this.content = null;
        }
        this.message = null;
        checkpoint(State.SKIP_CONTROL_CHARS);
        return message;
    }

    private void skipControlCharacters(ChannelBuffer buffer) {
        for (; ; ) {
            char c = (char) buffer.readUnsignedByte();
            if (!Character.isISOControl(c) && !Character.isWhitespace(c)) {
                buffer.readerIndex(buffer.readerIndex() - 1);
                break;
            }
        }
    }

    private void readFixedLengthContent(ChannelBuffer buffer) {
        long length = HttpHeaders.getContentLength(message, -1);
        assert length <= Integer.MAX_VALUE;
        if (content == null) {
            content = buffer.readBytes((int) length);
        } else {
            content.writeBytes(buffer.readBytes((int) length));
        }
    }

    private State readHeaders(ChannelBuffer buffer) throws TooLongFrameException {
        headerSize = 0;
        final HttpMessage message = this.message;
        String line = readHeader(buffer);
        String lastHeader = null;
        if (line.length() != 0) {
            message.clearHeaders();
            do {
                char firstChar = line.charAt(0);
                if (lastHeader != null && (firstChar == ' ' || firstChar == '\t')) {
                    List<String> current = message.getHeaders(lastHeader);
                    int lastPos = current.size() - 1;
                    String newString = current.get(lastPos) + line.trim();
                    current.set(lastPos, newString);
                } else {
                    String[] header = splitHeader(line);
                    message.addHeader(header[0], header[1]);
                    lastHeader = header[0];
                }
                line = readHeader(buffer);
            } while (line.length() != 0);
        }
        State nextState;
        if (isContentAlwaysEmpty(message)) {
            nextState = State.SKIP_CONTROL_CHARS;
        } else if (message.isChunked()) {
            nextState = State.READ_CHUNK_SIZE;
        } else if (HttpHeaders.getContentLength(message, -1) >= 0) {
            nextState = State.READ_FIXED_LENGTH_CONTENT;
        } else {
            nextState = State.READ_VARIABLE_LENGTH_CONTENT;
        }
        return nextState;
    }

    private HttpChunkTrailer readTrailingHeaders(ChannelBuffer buffer) throws TooLongFrameException {
        headerSize = 0;
        String line = readHeader(buffer);
        String lastHeader = null;
        if (line.length() != 0) {
            HttpChunkTrailer trailer = new DefaultHttpChunkTrailer();
            do {
                char firstChar = line.charAt(0);
                if (lastHeader != null && (firstChar == ' ' || firstChar == '\t')) {
                    List<String> current = trailer.getHeaders(lastHeader);
                    if (current.size() != 0) {
                        int lastPos = current.size() - 1;
                        String newString = current.get(lastPos) + line.trim();
                        current.set(lastPos, newString);
                    } else {
                    }
                } else {
                    String[] header = splitHeader(line);
                    String name = header[0];
                    if (!name.equalsIgnoreCase(HttpHeaders.Names.CONTENT_LENGTH) && !name.equalsIgnoreCase(HttpHeaders.Names.TRANSFER_ENCODING) && !name.equalsIgnoreCase(HttpHeaders.Names.TRAILER)) {
                        trailer.addHeader(name, header[1]);
                    }
                    lastHeader = name;
                }
                line = readHeader(buffer);
            } while (line.length() != 0);
            return trailer;
        }
        return HttpChunk.LAST_CHUNK;
    }

    private String readHeader(ChannelBuffer buffer) throws TooLongFrameException {
        StringBuilder sb = new StringBuilder(64);
        int headerSize = this.headerSize;
        loop: for (; ; ) {
            char nextByte = (char) buffer.readByte();
            headerSize++;
            switch(nextByte) {
                case HttpCodecUtil.CR:
                    nextByte = (char) buffer.readByte();
                    headerSize++;
                    if (nextByte == HttpCodecUtil.LF) {
                        break loop;
                    }
                    break;
                case HttpCodecUtil.LF:
                    break loop;
            }
            if (headerSize >= maxHeaderSize) {
                throw new TooLongFrameException("HTTP header is larger than " + maxHeaderSize + " bytes.");
            }
            sb.append(nextByte);
        }
        this.headerSize = headerSize;
        return sb.toString();
    }

    protected abstract boolean isDecodingRequest();

    protected abstract HttpMessage createMessage(String[] initialLine) throws Exception;

    private int getChunkSize(String hex) {
        hex = hex.trim();
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            if (c == ';' || Character.isWhitespace(c) || Character.isISOControl(c)) {
                hex = hex.substring(0, i);
                break;
            }
        }
        return Integer.parseInt(hex, 16);
    }

    private String readLine(ChannelBuffer buffer, int maxLineLength) throws TooLongFrameException {
        StringBuilder sb = new StringBuilder(64);
        int lineLength = 0;
        while (true) {
            byte nextByte = buffer.readByte();
            if (nextByte == HttpCodecUtil.CR) {
                nextByte = buffer.readByte();
                if (nextByte == HttpCodecUtil.LF) {
                    return sb.toString();
                }
            } else if (nextByte == HttpCodecUtil.LF) {
                return sb.toString();
            } else {
                if (lineLength >= maxLineLength) {
                    throw new TooLongFrameException("An HTTP line is larger than " + maxLineLength + " bytes.");
                }
                lineLength++;
                sb.append((char) nextByte);
            }
        }
    }

    private String[] splitInitialLine(String sb) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;
        aStart = findNonWhitespace(sb, 0);
        aEnd = findWhitespace(sb, aStart);
        bStart = findNonWhitespace(sb, aEnd);
        bEnd = findWhitespace(sb, bStart);
        cStart = findNonWhitespace(sb, bEnd);
        cEnd = findEndOfString(sb);
        return new String[] { sb.substring(aStart, aEnd), sb.substring(bStart, bEnd), cStart < cEnd ? sb.substring(cStart, cEnd) : "" };
    }

    private String[] splitHeader(String sb) {
        final int length = sb.length();
        int nameStart;
        int nameEnd;
        int colonEnd;
        int valueStart;
        int valueEnd;
        nameStart = findNonWhitespace(sb, 0);
        for (nameEnd = nameStart; nameEnd < length; nameEnd++) {
            char ch = sb.charAt(nameEnd);
            if (ch == ':' || Character.isWhitespace(ch)) {
                break;
            }
        }
        for (colonEnd = nameEnd; colonEnd < length; colonEnd++) {
            if (sb.charAt(colonEnd) == ':') {
                colonEnd++;
                break;
            }
        }
        valueStart = findNonWhitespace(sb, colonEnd);
        if (valueStart == length) {
            return new String[] { sb.substring(nameStart, nameEnd), "" };
        }
        valueEnd = findEndOfString(sb);
        return new String[] { sb.substring(nameStart, nameEnd), sb.substring(valueStart, valueEnd) };
    }

    private int findNonWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private int findWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result++) {
            if (Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    private int findEndOfString(String sb) {
        int result;
        for (result = sb.length(); result > 0; result--) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }
}
