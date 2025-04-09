package giftoapng;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import util.Byteutils;

/**
 * represents a chunk inside the PNG.
 * For more information look here: <a href="http://www.w3.org/TR/PNG/">PNG-specifcation</a>
 * @author Alexander Sch&auml;ffer
 *
 */
abstract class Chunk {

    /**
	 * holds the chunk-type p.e. "IHDR"
	 */
    protected Property mbaHeader;

    /**
	 * returns the byte[] with the data according to the chunk-type
	 * @return
	 */
    protected abstract byte[] getData();

    /**
	 * returns the 4 bytes of the chunktype
	 * @return
	 */
    protected byte[] getHeader() {
        return mbaHeader.getValue();
    }

    /**
	 * generates CRC32-checksum from badata
	 * @param badata
	 * @return
	 */
    protected byte[] getCRC32(byte[] badata) {
        CRC32 crcgen = new CRC32();
        crcgen.update(badata);
        return Byteutils.crc32tobytearr(crcgen.getValue());
    }

    /**
	 * writes the data like:
	 * 4 bytes length(x) of data, 4 bytes chunk-type,
	 * x bytes data, 4 bytes CRC32 from chunktype+data.
	 * The calling method should handle the exception.
	 * @param bos
	 * @throws IOException 
	 */
    public void write(BufferedOutputStream bos) throws IOException {
        byte[] chunktype = getHeader();
        byte[] data = getData();
        byte[] balength = Byteutils.inttobytearr(data.length);
        byte[] batmp = Byteutils.bytearrconcat(chunktype, data);
        byte[] bacrc = getCRC32(batmp);
        bos.write(balength);
        bos.write(batmp);
        bos.write(bacrc);
    }
}
