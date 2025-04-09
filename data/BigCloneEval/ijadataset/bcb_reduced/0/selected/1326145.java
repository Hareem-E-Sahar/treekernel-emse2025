package eu.more.cryptographicservicecore.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import eu.more.cryptographicservicecore.commons.Base64;
import eu.more.cryptographicservicecore.commons.SecurityException;
import eu.more.cryptographicservicecore.serializators.SerializationCore;

public class ZipCore {

    /**
	 * Compress the object with Zip algorithm
	 * 
	 * @param object
	 * @return
	 * @throws SecurityException
	 */
    public static byte[] zipToBytes(Object object) throws SecurityException {
        byte[] input = SerializationCore.serializeToBytes(object);
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);
        compressor.setInput(input);
        compressor.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }
        try {
            bos.close();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
        byte[] compressedData = bos.toByteArray();
        byte[] res = appendCRC32(compressedData);
        return res;
    }

    /**
	 * Compress the object with Zip algorithm
	 * 
	 * @param object
	 * @return
	 * @throws SecurityException
	 */
    public static String zip(Object object) throws SecurityException {
        byte[] compressedData = zipToBytes(object);
        return Base64.encodeBytes(compressedData);
    }

    /**
	 * Decompress a zipped byte array
	 * 
	 * @param compressedData
	 * @return
	 * @throws SecurityException
	 */
    public static Object unzipFromBytes(byte[] compressedData) throws SecurityException {
        byte[] input = verifyCRC32(compressedData);
        Inflater decompressor = new Inflater();
        decompressor.setInput(input);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            } catch (DataFormatException e) {
                throw new SecurityException(e);
            }
        }
        try {
            bos.close();
        } catch (IOException e) {
            throw new SecurityException(e);
        }
        byte[] decompressedData = bos.toByteArray();
        Object res = SerializationCore.deserialize(decompressedData);
        return res;
    }

    /**
	 * Decompress a zipped string
	 * 
	 * @param compressedData
	 * @return
	 * @throws SecurityException
	 */
    public static Object unzip(String compressedData) throws SecurityException {
        byte[] compressedDataArray = Base64.decode(compressedData);
        return unzipFromBytes(compressedDataArray);
    }

    /**
	 * Retrieves the CRC32 of a stream
	 * 
	 * @param element
	 * @return
	 */
    private static long crc32(byte[] stream) {
        CRC32 crc32 = new CRC32();
        crc32.reset();
        crc32.update(stream);
        long res = crc32.getValue();
        return res;
    }

    /**
	 * Retrieves the CRC32 of the stream and appends it in the first byte.
	 * 
	 * @param stream
	 * @return
	 */
    private static byte[] appendCRC32(byte[] stream) {
        byte[] crc32 = new byte[1];
        byte[] res = new byte[stream.length + 1];
        Array.setByte(crc32, 0, (byte) crc32(stream));
        System.arraycopy(crc32, 0, res, 0, 1);
        System.arraycopy(stream, 0, res, 1, stream.length);
        return res;
    }

    /**
	 * Checks the CRC32. If both crcs are identical, returns the stream,
	 * otherwise, raise an exception
	 * 
	 * @param stream
	 * @return
	 * @throws SecurityException
	 */
    private static byte[] verifyCRC32(byte[] stream) throws SecurityException {
        byte[] crc32 = new byte[1];
        byte[] originalStream = new byte[stream.length - 1];
        System.arraycopy(stream, 0, crc32, 0, 1);
        byte givenCRC = Array.getByte(crc32, 0);
        System.arraycopy(stream, 1, originalStream, 0, stream.length - 1);
        byte calculatedCRC = (byte) crc32(originalStream);
        boolean res = (givenCRC == calculatedCRC);
        if (!res) {
            throw new SecurityException("CSCORE: Zip's CRC32 check failed.");
        }
        return originalStream;
    }

    /**
	 * Determines if the given stream is a valid zip
	 * 
	 * @param stream
	 * @return
	 */
    public static boolean isValidZipStream(byte[] stream) {
        try {
            return (verifyCRC32(stream) != null);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
	 * Determines if the given stream is a valid zip
	 * 
	 * @param stream
	 * @return
	 */
    public static boolean isValidZipStream(String stream) {
        byte[] compressedDataArray = Base64.decode(stream);
        try {
            return (verifyCRC32(compressedDataArray) != null);
        } catch (SecurityException e) {
            return false;
        }
    }
}
