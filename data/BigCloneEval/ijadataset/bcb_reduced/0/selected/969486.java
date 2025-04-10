package org.jcvi.trace.fourFiveFour.flowgram.sff;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jcvi.testUtil.EasyMockUtil;
import org.jcvi.trace.fourFiveFour.flowgram.sff.SFFDecoderException;
import org.jcvi.trace.fourFiveFour.flowgram.sff.SFFReadHeader;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.jcvi.testUtil.EasyMockUtil.*;
import static org.easymock.EasyMock.*;

public class TestSFFReadHeaderCodec_decode extends AbstractTestSFFReadHeaderCodec {

    @Test
    public void valid() throws SFFDecoderException, IOException {
        InputStream mockInputStream = createMock(InputStream.class);
        encodeHeader(mockInputStream, expectedReadHeader);
        replay(mockInputStream);
        SFFReadHeader actualReadHeader = sut.decodeReadHeader(new DataInputStream(mockInputStream));
        assertEquals(actualReadHeader, expectedReadHeader);
        verify(mockInputStream);
    }

    @Test
    public void sequenceNameLengthEncodedIncorrectlyShouldThrowIOException() throws IOException {
        InputStream mockInputStream = createMock(InputStream.class);
        encodeHeaderWithWrongSequenceLength(mockInputStream, expectedReadHeader);
        replay(mockInputStream);
        try {
            sut.decodeReadHeader(new DataInputStream(mockInputStream));
            fail("should throw SFFDecoderException if name length encoded wrong");
        } catch (IOException e) {
            Throwable cause = e.getCause();
            assertEquals("error decoding seq name", cause.getMessage());
        }
        verify(mockInputStream);
    }

    @Test
    public void readThrowsIOExceptionShouldWrapInSFFDecoderException() throws IOException {
        InputStream mockInputStream = createMock(InputStream.class);
        IOException expectedIOException = new IOException("expected");
        expect(mockInputStream.read()).andThrow(expectedIOException);
        replay(mockInputStream);
        try {
            sut.decodeReadHeader(new DataInputStream(mockInputStream));
            fail("should wrap IOException in SFFDecoderException");
        } catch (SFFDecoderException e) {
            assertEquals("error trying to decode read header", e.getMessage());
            assertEquals(expectedIOException, e.getCause());
        }
        verify(mockInputStream);
    }

    void encodeHeader(InputStream mockInputStream, SFFReadHeader readHeader) throws IOException {
        final String seqName = readHeader.getName();
        final int nameLength = seqName.length();
        int unpaddedLength = 16 + nameLength;
        final long padds = SFFUtil.caclulatePaddedBytes(unpaddedLength);
        putShort(mockInputStream, (short) (padds + unpaddedLength));
        putShort(mockInputStream, (short) nameLength);
        putInt(mockInputStream, readHeader.getNumberOfBases());
        putShort(mockInputStream, (short) readHeader.getQualityClip().getLocalStart());
        putShort(mockInputStream, (short) readHeader.getQualityClip().getLocalEnd());
        putShort(mockInputStream, (short) readHeader.getAdapterClip().getLocalStart());
        putShort(mockInputStream, (short) readHeader.getAdapterClip().getLocalEnd());
        expect(mockInputStream.read(isA(byte[].class), eq(0), eq(nameLength))).andAnswer(EasyMockUtil.writeArrayToInputStream(seqName.getBytes()));
        expect(mockInputStream.skip(padds)).andReturn(padds);
    }

    void encodeHeaderWithWrongSequenceLength(InputStream mockInputStream, SFFReadHeader readHeader) throws IOException {
        final String seqName = readHeader.getName();
        final int nameLength = seqName.length();
        int unpaddedLength = 16 + nameLength;
        final long padds = SFFUtil.caclulatePaddedBytes(unpaddedLength);
        putShort(mockInputStream, (short) (padds + unpaddedLength));
        putShort(mockInputStream, (short) (nameLength + 1));
        putInt(mockInputStream, readHeader.getNumberOfBases());
        putShort(mockInputStream, (short) readHeader.getQualityClip().getStart());
        putShort(mockInputStream, (short) readHeader.getQualityClip().getEnd());
        putShort(mockInputStream, (short) readHeader.getAdapterClip().getStart());
        putShort(mockInputStream, (short) readHeader.getAdapterClip().getEnd());
        expect(mockInputStream.read(isA(byte[].class), eq(0), eq(nameLength + 1))).andAnswer(EasyMockUtil.writeArrayToInputStream(seqName.getBytes()));
    }
}
