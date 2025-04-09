package net.sf.jannot.tabix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Alternative to GZIPInputStream, for decompressing GZIP blocks that are
 * already loaded into a byte[]. The main advantage is that this object can be
 * used over and over again to decompress many blocks, whereas a new
 * GZIPInputStream and ByteArrayInputStream would otherwise need to be created
 * for each block to be decompressed.
 * 
 * This code requires that the GZIP header conform to the GZIP blocks written to
 * BAM files, with a specific subfield and no other optional stuff.
 * 
 * @author alecw@broadinstitute.org
 */
public class BlockGunzipper {

    private final Inflater inflater = new Inflater(true);

    private final CRC32 crc32 = new CRC32();

    /**
	 * Decompress GZIP-compressed data
	 * 
	 * @param uncompressedBlock
	 *            must be big enough to hold decompressed output.
	 * @param compressedBlock
	 *            compressed data starting at offset 0
	 * @param compressedLength
	 *            size of compressed data, possibly less than the size of the
	 *            buffer.
	 * @throws IOException
	 */
    void unzipBlock(byte[] uncompressedBlock, byte[] compressedBlock, int compressedLength) throws IOException {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(compressedBlock, 0, compressedLength);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            if (byteBuffer.get() != BlockCompressedStreamConstants.GZIP_ID1 || byteBuffer.get() != (byte) BlockCompressedStreamConstants.GZIP_ID2 || byteBuffer.get() != BlockCompressedStreamConstants.GZIP_CM_DEFLATE || byteBuffer.get() != BlockCompressedStreamConstants.GZIP_FLG) {
                throw new IOException("Invalid GZIP header");
            }
            byteBuffer.position(byteBuffer.position() + 6);
            if (byteBuffer.getShort() != BlockCompressedStreamConstants.GZIP_XLEN) {
                throw new IOException("Invalid GZIP header");
            }
            byteBuffer.position(byteBuffer.position() + 4);
            final int totalBlockSize = (byteBuffer.getShort() & 0xffff) + 1;
            if (totalBlockSize != compressedLength) {
                throw new IOException("GZIP blocksize disagreement");
            }
            final int deflatedSize = compressedLength - BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH - BlockCompressedStreamConstants.BLOCK_FOOTER_LENGTH;
            byteBuffer.position(byteBuffer.position() + deflatedSize);
            int expectedCrc = byteBuffer.getInt();
            int uncompressedSize = byteBuffer.getInt();
            inflater.reset();
            inflater.setInput(compressedBlock, BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH, deflatedSize);
            final int inflatedBytes = inflater.inflate(uncompressedBlock, 0, uncompressedSize);
            if (inflatedBytes != uncompressedSize) {
                throw new IOException("Did not inflate expected amount");
            }
            crc32.reset();
            crc32.update(uncompressedBlock, 0, uncompressedSize);
            final long crc = crc32.getValue();
            if ((int) crc != expectedCrc) {
                throw new IOException("CRC mismatch");
            }
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        }
    }
}
