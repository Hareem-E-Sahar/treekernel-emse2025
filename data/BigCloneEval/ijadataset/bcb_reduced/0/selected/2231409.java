package com.go.teaservlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.zip.CRC32;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import com.go.teaservlet.util.FilteredHttpServletResponse;
import com.go.teaservlet.io.CharToByteBuffer;
import com.go.teaservlet.io.InternedCharToByteBuffer;
import com.go.trove.io.ByteData;
import com.go.trove.io.ByteBuffer;
import com.go.trove.io.DefaultByteBuffer;
import com.go.trove.io.ByteBufferOutputStream;
import com.go.trove.io.CharToByteBufferWriter;
import com.go.trove.log.Log;
import com.go.trove.util.Deflater;
import com.go.trove.util.DeflaterPool;
import com.go.trove.util.DeflaterOutputStream;
import com.go.tea.runtime.OutputReceiver;
import com.go.tea.runtime.Substitution;

/******************************************************************************
 * A HttpServletResponse wrapper that tracks whether a redirect or error will
 * be sent to the client.
 *
 * @author Reece Wilton, Brian S O'Neill
 * @version
 * <!--$$Revision: 3 $-->, <!--$$JustDate:-->  3/04/02 <!-- $-->
 */
class ApplicationResponseImpl extends FilteredHttpServletResponse implements ApplicationResponse {

    private static final byte[] GZIP_HEADER = { (byte) 0x1f, (byte) 0x8b, (byte) Deflater.DEFLATED, 0, 0, 0, 0, 0, 0, 0 };

    private static final byte[] FINAL_BLANK_HEADER = { 1, 0, 0, (byte) 0xff, (byte) 0xff };

    static void writeShort(OutputStream out, int i) throws IOException {
        out.write((byte) i);
        out.write((byte) (i >> 8));
    }

    static void writeInt(OutputStream out, int i) throws IOException {
        out.write((byte) i);
        out.write((byte) (i >> 8));
        out.write((byte) (i >> 16));
        out.write((byte) (i >> 24));
    }

    protected final Log mLog;

    protected final CharToByteBuffer mBuffer;

    protected int mState;

    private HttpContext mHttpContext;

    private final TeaServletEngineImpl mTeaServletEngine;

    private ApplicationRequest mRequest;

    private ServletOutputStream mOut;

    private PrintWriter mWriter;

    private int mCompressedSegments;

    ApplicationResponseImpl(HttpServletResponse response, TeaServletEngineImpl engine) throws IOException {
        this(response, engine, new DefaultByteBuffer());
    }

    ApplicationResponseImpl(HttpServletResponse response, TeaServletEngineImpl engine, ByteBuffer bb) throws IOException {
        super(response);
        mTeaServletEngine = engine;
        mLog = mTeaServletEngine.getLog();
        String encoding = response.getCharacterEncoding();
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }
        CharToByteBuffer buffer = new FastCharToByteBuffer(bb, encoding);
        mBuffer = new InternedCharToByteBuffer(buffer);
    }

    public ServletOutputStream getOutputStream() {
        if (mOut == null) {
            final OutputStream out = new ByteBufferOutputStream(mBuffer);
            mOut = new ServletOutputStream() {

                public void write(int b) throws IOException {
                    out.write(b);
                }

                public void write(byte[] buf) throws IOException {
                    out.write(buf);
                }

                public void write(byte[] buf, int off, int len) throws IOException {
                    out.write(buf, off, len);
                }
            };
        }
        return mOut;
    }

    public PrintWriter getWriter() throws IOException {
        if (mWriter == null) {
            Writer w = new CharToByteBufferWriter(mBuffer);
            mWriter = new PrintWriter(w, false);
        }
        return mWriter;
    }

    public void setContentLength(int len) {
    }

    public void setContentType(String type) {
        super.setContentType(type);
        try {
            mBuffer.setEncoding(getCharacterEncoding());
        } catch (IOException e) {
            Thread t = Thread.currentThread();
            t.getThreadGroup().uncaughtException(t, e);
        }
    }

    public void setBufferSize(int size) {
    }

    public int getBufferSize() {
        return Integer.MAX_VALUE;
    }

    public void flushBuffer() {
    }

    public void resetBuffer() {
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
        super.reset();
        resetBuffer();
    }

    public void setLocale(Locale locale) {
        super.setLocale(locale);
        mHttpContext.setLocale(locale);
    }

    public void sendError(int statusCode, String msg) throws IOException {
        mState |= 1;
        super.sendError(statusCode, msg);
    }

    public void sendError(int statusCode) throws IOException {
        mState |= 1;
        super.sendError(statusCode);
    }

    public void sendRedirect(String location) throws IOException {
        mState |= 1;
        super.sendRedirect(location);
    }

    public boolean isRedirectOrError() {
        return (mState & 1) != 0;
    }

    public CharToByteBuffer getResponseBuffer() {
        return mBuffer;
    }

    public HttpContext getHttpContext() {
        return mHttpContext;
    }

    public void stealOutput(Substitution s, OutputReceiver receiver) throws Exception {
        ((HttpContext) mHttpContext).stealOutput(receiver, s);
    }

    public DetachedData execDetached(Substitution s) throws Exception {
        DetachedResponseImpl response = new DetachedResponseImpl(mResponse, mTeaServletEngine);
        HttpContext newContext = (HttpContext) mTeaServletEngine.createHttpContext(mRequest, response);
        HttpContext thisContext = mHttpContext;
        response.setRequestAndHttpContext(newContext, mRequest);
        newContext.setLocale(thisContext.getLocale());
        newContext.nullFormat(thisContext.getNullFormat());
        newContext.dateFormat(thisContext.getDateFormat(), thisContext.getDateFormatTimeZone());
        newContext.numberFormat(thisContext.getNumberFormat(), thisContext.getNumberFormatInfinity(), thisContext.getNumberFormatNaN());
        try {
            s.detach().substitute(newContext);
        } catch (AbortTemplateException e) {
        }
        return response.getData();
    }

    public DetachedData execDetached(Command command) throws Exception {
        DetachedResponseImpl response = new DetachedResponseImpl(mResponse, mTeaServletEngine);
        HttpContext newContext = (HttpContext) mTeaServletEngine.createHttpContext(mRequest, response);
        HttpContext thisContext = mHttpContext;
        response.setRequestAndHttpContext(newContext, mRequest);
        newContext.setLocale(thisContext.getLocale());
        newContext.nullFormat(thisContext.getNullFormat());
        newContext.dateFormat(thisContext.getDateFormat(), thisContext.getDateFormatTimeZone());
        newContext.numberFormat(thisContext.getNumberFormat(), thisContext.getNumberFormatInfinity(), thisContext.getNumberFormatNaN());
        command.execute(mRequest, response);
        return response.getData();
    }

    public boolean insertCommand(Command command) throws Exception {
        return false;
    }

    public void finish() throws IOException {
        if (mState != 0) {
            return;
        }
        mState |= 2;
        ByteData bytes = mBuffer;
        OutputStream out = super.getOutputStream();
        long length = bytes.getByteCount();
        try {
            if (mCompressedSegments == 0 || length > 0xffffffffL) {
                if (length <= Integer.MAX_VALUE) {
                    super.setContentLength((int) length);
                }
                bytes.writeTo(out);
                return;
            }
            setHeader("Content-Encoding", "gzip");
            LengthComputer lc = new LengthComputer(mCompressedSegments * 2 + 1);
            bytes.writeTo(lc);
            lc.nextSegment(false);
            int contentLength = lc.getLength() + 10 + 5 + 8;
            super.setContentLength(contentLength);
            out.write(GZIP_HEADER);
            bytes.writeTo(new FinalOut(out, lc.mSegments));
            out.write(FINAL_BLANK_HEADER);
            writeInt(out, computeCRC(bytes));
            writeInt(out, (int) length);
        } finally {
            try {
                bytes.reset();
            } catch (IOException e) {
                mLog.warn(e);
            }
        }
    }

    void setRequestAndHttpContext(HttpContext context, ApplicationRequest req) {
        mHttpContext = context;
        mRequest = req;
    }

    void appendCompressed(ByteData compressed, ByteData original) throws IOException {
        mCompressedSegments++;
        mBuffer.appendSurrogate(new CompressedData(compressed, original));
    }

    private int computeCRC(ByteData bytes) throws IOException {
        final CRC32 crc = new CRC32();
        OutputStream out = new OutputStream() {

            public void write(int b) {
                crc.update(b);
            }

            public void write(byte[] b) {
                crc.update(b, 0, b.length);
            }

            public void write(byte[] b, int off, int len) {
                crc.update(b, off, len);
            }
        };
        bytes.writeTo(out);
        return (int) crc.getValue();
    }

    private static class CompressedData implements ByteData {

        private final ByteData mCompressed;

        private final ByteData mOriginal;

        CompressedData(ByteData compressed, ByteData original) {
            mCompressed = compressed;
            mOriginal = original;
        }

        public long getByteCount() throws IOException {
            return mOriginal.getByteCount();
        }

        public void writeTo(OutputStream out) throws IOException {
            if (out instanceof Segmented) {
                ((Segmented) out).nextSegment(true);
                mCompressed.writeTo(out);
                ((Segmented) out).nextSegment(false);
            } else {
                mOriginal.writeTo(out);
            }
        }

        public void reset() throws IOException {
            mOriginal.reset();
        }
    }

    private interface Segmented {

        void nextSegment(boolean preCompressed);
    }

    private static class LengthComputer extends OutputStream implements Segmented {

        int[] mSegments;

        int mSegCount;

        private int mCurrentSegment;

        private int mSign = 1;

        LengthComputer(int segmentCount) {
            mSegments = new int[segmentCount];
        }

        public void write(int b) {
            mCurrentSegment += mSign;
        }

        public void write(byte[] b) {
            mCurrentSegment += b.length * mSign;
        }

        public void write(byte[] b, int off, int len) {
            mCurrentSegment += len * mSign;
        }

        public void nextSegment(boolean preCompressed) {
            mSegments[mSegCount++] = mCurrentSegment;
            mSign = preCompressed ? -1 : 1;
            mCurrentSegment = 0;
        }

        int getLength() {
            int length = 0;
            for (int i = 0; i < mSegCount; i++) {
                int segLen = mSegments[i];
                if (segLen < 0) {
                    length += -segLen;
                } else {
                    length += segLen + ((65534 + segLen) / 65535) * 5;
                }
            }
            return length;
        }
    }

    private static class FinalOut extends OutputStream implements Segmented {

        private OutputStream mFinOut;

        private int[] mSegments;

        private int mCursor;

        private int mBlockLen;

        FinalOut(OutputStream out, int[] segments) {
            mFinOut = out;
            mSegments = segments;
        }

        public void write(int b) throws IOException {
            if (mBlockLen < 0) {
                mFinOut.write(b);
                return;
            }
            if (nextBlock(1) > 0) {
                mFinOut.write(b);
                mBlockLen--;
            }
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            if (mBlockLen < 0) {
                mFinOut.write(b, off, len);
                return;
            }
            while (len > 0) {
                int amt = nextBlock(len);
                if (amt <= 0) {
                    break;
                }
                mFinOut.write(b, off, amt);
                len -= amt;
                off += amt;
                mBlockLen -= amt;
            }
        }

        public void nextSegment(boolean preCompressed) {
            mCursor++;
            mBlockLen = preCompressed ? -1 : 0;
        }

        private int nextBlock(int needed) throws IOException {
            if (mBlockLen > 0) {
                return mBlockLen <= needed ? mBlockLen : needed;
            }
            int blockLen = mSegments[mCursor];
            if (blockLen > 65535) {
                blockLen = 65535;
            }
            mSegments[mCursor] -= blockLen;
            mFinOut.write(0);
            writeShort(mFinOut, blockLen);
            writeShort(mFinOut, 65535 - blockLen);
            mBlockLen = blockLen;
            return blockLen < needed ? blockLen : needed;
        }
    }
}
