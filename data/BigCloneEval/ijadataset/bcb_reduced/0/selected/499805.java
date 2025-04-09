package org.opennms.protocols.snmp.asn1;

import java.lang.*;
import java.math.BigInteger;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;
import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;

/**
 * The BerEncoder class is used to implement the AsnEncoder interface for
 * the Basic Encoding Rules (BER). The encoding rules are used
 * to encode and decode SNMP values using BER. 
 *
 * @author	<a href="http://www.opennms.org">OpenNMS</a>
 * @author	<a href="mailto:weave@opennms.org">Brian Weaver</a>
 * @version	$Revision: 1.8 $
 */
public class BerEncoder implements AsnEncoder {

    /**
	 * Defines the ASN.1 long length marker for the
	 * Basic Encoding Rule (BER)
	 */
    private static final byte LONG_LENGTH = (byte) 0x80;

    /**
	 * Defines the "high bit" that is the sign extension 
	 * bit for a 8-bit signed value.
	 */
    private static final byte HIGH_BIT = (byte) 0x80;

    /**
	 * Defines the BER extension "value" that is used
	 * to mark an extension type.
	 */
    private static final byte EXTENSION_ID = (byte) 0x1F;

    /**
	 * Defines the BER constructor id
	 */
    private static final byte CONSTRUCTOR = (byte) 0x20;

    /**
	 * Converts a primitive byte to a primitive long
	 * using "unsigned" logic.
	 *
	 * @param b The 8-bit value to convert
	 * @return Returns the 32-bit converted value
	 *
	 */
    protected static int byteToInt(byte b) {
        return (b < 0) ? 256 + (int) b : (int) b;
    }

    /**
	 * Converts a primitive byte to a primitive long.
	 * The byte is converted using "unsigned" logic
	 *
	 * @param b The 8-bit value to convert
	 * @return Returns the 64-bit converted value
	 *
	 */
    protected static long byteToLong(byte b) {
        return (b < 0) ? 256 + (long) b : (long) b;
    }

    /**
	 * Used to determine if the ASN.1 type is a constructor.
	 *
	 * @param b The ASN.1 type
	 *
	 * @return True if the ASN.1 type is a constructor, otherwise
	 *	a false is returned.
	 *
	 */
    protected static boolean isConstructor(byte b) {
        return ((b & CONSTRUCTOR) == CONSTRUCTOR);
    }

    /**
	 * Used to test if the ASN.1 type is an extension.
	 *
	 * @param b The ASN.1 type.
	 *
	 * @return True if the ASN.1 type is an extension. False
	 *	if the ASN.1 type is not an extension.
	 *
	 */
    protected static boolean isExtensionId(byte b) {
        return ((b & EXTENSION_ID) == EXTENSION_ID);
    }

    /**
	 * Used to copy data from one buffer to another. The method has the 
	 * flexability to allow the caller to specify an offset in each buffer
	 * and the total number of bytes to copy
	 *
	 * @param src		The source buffer
	 * @param srcOff	The offset of the first byte in the source buffer
	 * @param dest		The destination buffer
	 * @param destOff	The offset of the first byte in the destination buffer
	 * @param intsToCopy	The number of integer elements to copy
	 *
	 * @exception ArrayIndexOutOfBoundsException Thrown if there is insufficent
	 *		space in either array to copy the data.
	 *
	 */
    protected static void copy(byte[] src, int srcOff, byte[] dest, int destOff, int bytesToCopy) throws ArrayIndexOutOfBoundsException {
        if ((dest.length - destOff) < bytesToCopy || (src.length - srcOff) < bytesToCopy) throw new ArrayIndexOutOfBoundsException("Destination or source buffer is insufficent");
        for (int x = bytesToCopy - 1; x >= 0; x--) {
            dest[destOff + x] = src[srcOff + x];
        }
    }

    /**
	 * Used to copy data from one buffer to another. The method has the 
	 * flexability to allow the caller to specify an offset in each buffer
	 * and the total number of integers to copy
	 *
	 * @param src		The source buffer
	 * @param srcOff	The offset of the first integer in the source buffer
	 * @param dest		The destination buffer
	 * @param destOff	The offset of the first integer in the destination buffer
	 * @param intsToCopy	The number of integer elements to copy
	 *
	 * @exception ArrayIndexOutOfBoundsException Thrown if there is insufficent
	 *		space in either array to copy the data.
	 *
	 */
    protected static void copy(int[] src, int srcOff, int[] dest, int destOff, int intsToCopy) throws ArrayIndexOutOfBoundsException {
        if ((dest.length - destOff) < intsToCopy || (src.length - srcOff) < intsToCopy) throw new ArrayIndexOutOfBoundsException("Destination or source buffer is insufficent");
        for (int x = intsToCopy - 1; x >= 0; x--) {
            dest[destOff + x] = src[srcOff + x];
        }
    }

    /**
	 * Rotates a give buffer area marked by begin, pivot, and end.
	 * The pivot marks the point where the bytes between [pivot..end)
	 * are moved to the position marked by begin. The bytes between
	 * [begin..pivot) are shifted such that begin is at [begin+(end-pivot)].
	 *
	 * @param buf	The buffer containing the data to rotate
	 * @param begin	The start of the rotation
	 * @param pivot	The pivot point for the rotation
	 * @param end	The end of the rotational buffer
	 *
	 * @exception	ArrayIndexOutOfBoundsException	Thrown if an access exception occurs
	 *
	 */
    protected static void rotate(byte[] buf, int begin, int pivot, int end) throws ArrayIndexOutOfBoundsException {
        int dist = end - pivot;
        byte[] hold = new byte[dist];
        copy(buf, pivot, hold, 0, dist);
        for (int x = (pivot - begin) - 1; x >= 0; x--) {
            buf[begin + dist + x] = buf[begin + x];
        }
        copy(hold, 0, buf, begin, dist);
    }

    /** 
	 * Default constructor for the BER Encoder.
	 * 
	 */
    public BerEncoder() {
    }

    /**
	 *
	 * The buildLength() method is used to encode an ASN.1 length
	 * into the specified byte buffer. The method is defined in
	 * the AsnEncoder interface.
	 *
	 * @param buf		The output buffer of encoded bytes.
	 * @param startOffset	The offset from the start of the buffer where the 
	 *			method should start writing the encoded data.
	 * @param asnLength	The length to be encoded.
	 *
	 * @return	Returns the new offset for the next encoding routine.
	 *		If the startOffset is subtracted from the return value
	 *		then the length of the encoded data can be determined.
	 *
	 * @exception	AsnEncodingException	Thrown if an error occurs encoding
	 *			the datatype.
	 */
    public int buildLength(byte[] buf, int startOffset, int asnLength) throws AsnEncodingException {
        if (asnLength <= 0x7f) {
            if ((buf.length - startOffset) < 1) throw new AsnEncodingException("Buffer overflow error");
            buf[startOffset++] = (byte) (asnLength & 0x7f);
        } else if (asnLength <= 0xff) {
            if ((buf.length - startOffset) < 2) throw new AsnEncodingException("Buffer overflow error");
            buf[startOffset++] = (byte) (0x01 | LONG_LENGTH);
            buf[startOffset++] = (byte) (asnLength & 0xff);
        } else {
            if ((buf.length - startOffset) < 3) throw new AsnEncodingException("Buffer overflow error");
            buf[startOffset++] = (byte) (0x02 | LONG_LENGTH);
            buf[startOffset++] = (byte) ((asnLength >>> 8) & 0xff);
            buf[startOffset++] = (byte) (asnLength & 0xff);
        }
        return startOffset;
    }

    /**
	 * 
	 * The parseLength() method is used to decode an ASN.1 length
	 * from the specified buffer. The method is defined to support the
	 * AsnEncoder interface. 
	 *
	 * @param buf		The input buffer
	 * @param startOffset	The offset to start decoding in the buffer
	 *
	 * @return	Returns an Object array that contains the new offset and
	 *		the decoded length. The first object is an Integer object
	 *		and contains the new offset for the next object in buf.
	 *		The second object is an Integer and contains the actual
	 *		decoded length.
	 *
	 * @exception	AsnDecodingException	Thrown if an error occurs decoding
	 *			the buffer.
	 */
    public Object[] parseLength(byte[] buf, int startOffset) throws AsnDecodingException {
        if ((buf.length - startOffset) < 1) throw new AsnDecodingException("Buffer underflow error");
        Object[] retVals = new Object[2];
        byte numBytes = buf[startOffset++];
        if ((numBytes & LONG_LENGTH) == 0) {
            numBytes = (byte) (numBytes & ~LONG_LENGTH);
            retVals[1] = new Integer(byteToInt(numBytes));
        } else {
            numBytes = (byte) (numBytes & ~LONG_LENGTH);
            if (numBytes == 1) {
                if ((buf.length - startOffset) < 1) throw new AsnDecodingException("Buffer underflow error");
                retVals[1] = new Integer(byteToInt(buf[startOffset++]));
            } else if (numBytes == 2) {
                if ((buf.length - startOffset) < 2) throw new AsnDecodingException("Buffer underflow error");
                int val = byteToInt(buf[startOffset++]) << 8 | byteToInt(buf[startOffset++]);
                retVals[1] = new Integer(val);
            } else {
                throw new AsnDecodingException("Invalid ASN.1 length");
            }
        }
        retVals[0] = new Integer(startOffset);
        return retVals;
    }

    /**
	 *
	 * The buildHeader() method is used to encode an ASN.1 header
	 * into the specified byte buffer. The method is defined to
	 * support the AsnEncoder interface. This method is dependant
	 * on the buildLength() method.
	 *
	 * @param buf		The output buffer of encoded bytes.
	 * @param startOffset	The offset from the start of the buffer where the 
	 *			method should start writing the encoded data.
	 * @param asnType	The ASN.1 type to place in the buffer
	 * @param asnLength	The length to be encoded.
	 *
	 * @return	Returns the new offset for the next encoding routine.
	 *		If startOffset is subtracted from the return value
	 *		then the length of the encoded data can be determined.
	 *
	 * @exception	AsnEncodingException	Thrown if an error occurs encoding
	 *			the datatype.
	 * 
	 */
    public int buildHeader(byte[] buf, int startOffset, byte asnType, int asnLength) throws AsnEncodingException {
        if ((buf.length - startOffset) < 1) throw new AsnEncodingException("Buffer overflow error");
        buf[startOffset++] = asnType;
        return buildLength(buf, startOffset, asnLength);
    }

    /**
	 * 
	 * The parseHeader() method is used to decode an ASN.1 header
	 * from the specified buffer. The method is defined to support
	 * the AsnEncoder interface. The method also calls the parseLength()
	 * method.
	 *
	 * @param buf		The input buffer
	 * @param startOffset	The offset to start decoding in the buffer
	 *
	 * @return	Returns an Object array that contains the new offset,
	 *		ASN.1 type, and decoded length. The first object is an
	 *		Integer object and contains the new offset for the 
	 *		next object in buf. The second object is a Byte object 
	 *		that represents the decoded ASN.1 Type. The third object 
	 *		is an Integer and contains the actual decoded length.
	 *
	 * @exception	AsnDecodingException	Thrown if an error occurs decoding
	 *			the buffer.
	 */
    public Object[] parseHeader(byte[] buf, int startOffset) throws AsnDecodingException {
        if ((buf.length - startOffset) < 1) throw new AsnDecodingException("Insufficent buffer length");
        byte asnType = buf[startOffset++];
        if (isExtensionId(asnType)) throw new AsnDecodingException("Buffer underflow error");
        Object[] lenVals = parseLength(buf, startOffset);
        Object[] rVals = new Object[3];
        rVals[0] = lenVals[0];
        rVals[1] = new Byte(asnType);
        rVals[2] = lenVals[1];
        return rVals;
    }

    /**
	 *
	 * The buildInteger32() method is used to encode an ASN.1 32-bit signed
	 * integer into the specified byte buffer. 
	 *
	 * @param buf		The output buffer of encoded bytes.
	 * @param startOffset	The offset from the start of the buffer where the 
	 *			method should start writing the encoded data.
	 * @param asnType	The ASN.1 type to place in the buffer
	 * @param asnInt32	The 32-bit signed integer to encode.
	 *
	 * @return	Returns the new offset for the next encoding routine.
	 *		If startOffset is subtracted from the return value
	 *		then the length of the encoded data can be determined.
	 *
	 * @exception	AsnEncodingException	Thrown if an error occurs encoding
	 *			the datatype.
	 * 
	 */
    public int buildInteger32(byte[] buf, int startOffset, byte asnType, int asnInt32) throws AsnEncodingException {
        int mask = 0xff800000;
        int intSz = 4;
        while (((asnInt32 & mask) == 0 || (asnInt32 & mask) == mask) && intSz > 1) {
            --intSz;
            asnInt32 = (asnInt32 << 8);
        }
        startOffset = buildHeader(buf, startOffset, asnType, intSz);
        if ((buf.length - startOffset) < intSz) throw new AsnEncodingException("Insufficent buffer size");
        mask = 0xff000000;
        while (intSz-- > 0) {
            byte b = (byte) ((asnInt32 & mask) >>> 24);
            buf[startOffset++] = b;
            asnInt32 = (asnInt32 << 8);
        }
        return startOffset;
    }

    /**
	 * 
	 * The parseInteger32() method is used to decode an ASN.1 32-bit signed
	 * integer from the specified buffer.
	 *
	 * @param buf		The input buffer
	 * @param startOffset	The offset to start decoding in the buffer
	 *
	 * @return	Returns an Object array that contains the new offset,
	 *		ASN.1 type, and value. The first object is an Integer object
	 *		and contains the new offset for the next object in buf.
	 *		The second object is a Byte object that represents the 
	 *		decoded ASN.1 Type. The third object is an Integer 
	 *		and contains the actual	decoded value.
	 *
	 * @exception	AsnDecodingException	Thrown if an error occurs decoding
	 *			the buffer.
	 */
    public Object[] parseInteger32(byte[] buf, int startOffset) throws AsnDecodingException {
        Object[] hdrVals = parseHeader(buf, startOffset);
        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = (Byte) hdrVals[1];
        int asnLength = ((Integer) hdrVals[2]).intValue();
        if ((buf.length - startOffset) < asnLength) throw new AsnDecodingException("Buffer underflow error");
        if (asnLength > 4) throw new AsnDecodingException("Integer too large: cannot decode");
        int asnValue = 0;
        if ((buf[startOffset] & HIGH_BIT) == HIGH_BIT) asnValue = -1;
        while (asnLength-- > 0) {
            asnValue = (asnValue << 8) | byteToInt(buf[startOffset++]);
        }
        Object[] rVals = new Object[3];
        rVals[0] = new Integer(startOffset);
        rVals[1] = asnType;
        rVals[2] = new Integer(asnValue);
        return rVals;
    }

    /**
	 *
	 * The buildUInteger32() method is used to encode an ASN.1 32-bit unsigned
	 * integer into the specified byte buffer.
	 *
	 * @param buf		The output buffer of encoded bytes.
	 * @param startOffset	The offset from the start of the buffer where the 
	 *			method should start writing the encoded data.
	 * @param asnType	The ASN.1 type to place in the buffer
	 * @param asnUInt32	The 32-bit unsigned integer to encode.
	 *
	 * @return	Returns the new offset for the next encoding routine.
	 *		If startOffset is subtracted from the return value
	 *		then the length of the encoded data can be determined.
	 *
	 * @exception	AsnEncodingException	Thrown if an error occurs encoding
	 *			the datatype.
	 * 
	 */
    public int buildUInteger32(byte[] buf, int startOffset, byte asnType, long asnUInt32) throws AsnEncodingException {
        long mask = 0xff800000L;
        int intSz = 4;
        boolean bAddNullByte = false;
        if (asnUInt32 > (long) (Integer.MAX_VALUE)) {
            bAddNullByte = true;
            intSz++;
        }
        while ((asnUInt32 & mask) == 0 && intSz > 1) {
            --intSz;
            asnUInt32 = (asnUInt32 << 8);
        }
        startOffset = buildHeader(buf, startOffset, asnType, intSz);
        if ((buf.length - startOffset) < intSz) throw new AsnEncodingException("Buffer overflow error");
        if (bAddNullByte) {
            buf[startOffset++] = (byte) 0;
            --intSz;
        }
        mask = 0xff000000L;
        while (intSz-- > 0) {
            byte b = (byte) ((asnUInt32 & mask) >>> 24);
            buf[startOffset++] = b;
            asnUInt32 = (asnUInt32 << 8);
        }
        return startOffset;
    }

    /**
	 * 
	 * The parseUInteger32() method is used to decode an ASN.1 32-bit unsigned
	 * integer from the specified buffer.
	 *
	 * @param buf		The input buffer
	 * @param startOffset	The offset to start decoding in the buffer
	 *
	 * @return	Returns an Object array that contains the new offset,
	 *		ASN.1 type, and value. The first object is an Integer object
	 *		and contains the new offset for the next object in buf.
	 *		The second object is a Byte object that represents the 
	 *		decoded ASN.1 Type. The third object is a Long object 
	 *		and contains the actual	decoded value.
	 *
	 * @exception	AsnDecodingException	Thrown if an error occurs decoding
	 *			the buffer.
	 */
    public Object[] parseUInteger32(byte[] buf, int startOffset) throws AsnDecodingException {
        Object[] hdrVals = parseHeader(buf, startOffset);
        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = (Byte) hdrVals[1];
        int asnLength = ((Integer) hdrVals[2]).intValue();
        if ((buf.length - startOffset) < asnLength) throw new AsnDecodingException("Buffer underflow error");
        if (asnLength > 5) throw new AsnDecodingException("Integer too large: cannot decode");
        long asnValue = 0;
        if ((buf[startOffset] & HIGH_BIT) == HIGH_BIT) asnValue = -1;
        while (asnLength-- > 0) {
            asnValue = (asnValue << 8) | byteToLong(buf[startOffset++]);
        }
        asnValue = (asnValue & 0xffffffffL);
        Object[] rVals = new Object[3];
        rVals[0] = new Integer(startOffset);
        rVals[1] = asnType;
        rVals[2] = new Long(asnValue);
        return rVals;
    }

    /**
	 *
	 * The buildUInteger64() method is used to encode an ASN.1 64-bit unsigned
	 * integer into the specified byte buffer.
	 *
	 * @param buf		The output buffer of encoded bytes.
	 * @param startOffset	The offset from the start of the buffer where the 
	 *			method should start writing the encoded data.
	 * @param asnType	The ASN.1 type to place in the buffer
	 * @param asnUInt64	The 64-bit unsigned integer to encode.
	 *
	 * @return	Returns the new offset for the next encoding routine.
	 *		If startOffset is subtracted from the return value
	 *		then the length of the encoded data can be determined.
	 *
	 * @exception	AsnEncodingException	Thrown if an error occurs encoding
	 *			the datatype.
	 * 
	 */
    public int buildUInteger64(byte[] buf, int startOffset, byte asnType, BigInteger asnUInt64) throws AsnEncodingException {
        byte[] bytes = asnUInt64.toByteArray();
        startOffset = buildHeader(buf, startOffset, asnType, bytes.length);
        if ((buf.length - startOffset) < bytes.length) throw new AsnEncodingException("Buffer overflow error");
        for (int i = 0; i < bytes.length; ++i) buf[startOffset++] = bytes[i];
        return startOffset;
    }

    /**
	 * 
	 * The parseUInteger64() method is used to decode an ASN.1 64-bit unsigned
	 * integer from the specified buffer.
	 *
	 * @param buf		The input buffer
	 * @param startOffset	The offset to start decoding in the buffer
	 *
	 * @return	Returns an Object array that contains the new offset,
	 *		ASN.1 type, and value. The first object is an Integer object
	 *		and contains the new offset for the next object in buf.
	 *		The second object is a Byte object that represents the 
	 *		decoded ASN.1 Type. The third object is a Long object 
	 *		and contains the actual	decoded value.
	 *
	 * @exception	AsnDecodingException	Thrown if an error occurs decoding
	 *			the buffer.
	 */
    public Object[] parseUInteger64(byte[] buf, int startOffset) throws AsnDecodingException {
        Object[] hdrVals = parseHeader(buf, startOffset);
        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = (Byte) hdrVals[1];
        int asnLength = ((Integer) hdrVals[2]).intValue();
        if ((buf.length - startOffset) < asnLength) throw new AsnDecodingException("Buffer underflow error");
        if (asnLength > 9) throw new AsnDecodingException("Integer too large: cannot decode");
        byte[] asnBuf = new byte[asnLength];
        for (int i = 0; i < asnLength; ++i) asnBuf[i] = buf[startOffset++];
        BigInteger asnValue = new BigInteger(asnBuf);
        Object[] rVals = new Object[3];
        rVals[0] = new Integer(startOffset);
        rVals[1] = asnType;
        rVals[2] = asnValue;
        return rVals;
    }

    /**
	 *
	 * The buildNull() method is used to encode an ASN.1 NULL value
	 * into the specified byte buffer.
	 *
	 * @param buf		The output buffer of encoded bytes.
	 * @param startOffset	The offset from the start of the buffer where the 
	 *			method should start writing the encoded data.
	 * @param asnType	The ASN.1 type to place in the buffer
	 *
	 * @return	Returns the new offset for the next encoding routine.
	 *		If startOffset is subtracted from the return value
	 *		then the length of the encoded data can be determined.
	 *
	 * @exception	AsnEncodingException	Thrown if an error occurs encoding
	 *			the datatype.
	 * 
	 */
    public int buildNull(byte[] buf, int startOffset, byte asnType) throws AsnEncodingException {
        return buildHeader(buf, startOffset, asnType, 0);
    }

    /**
	 * 
	 * The parseNull() method is used to decode an ASN.1 Null value
	 * from the specified buffer. Since there is no "null" value
	 * only the new offset and ASN.1 type are returned.
	 *
	 * @param buf		The input buffer
	 * @param startOffset	The offset to start decoding in the buffer
	 *
	 * @return	Returns an Object array that contains the new offset and
	 *		the ASN.1 type. The first object is an Integer object
	 *		and contains the new offset for the next object in buf.
	 *		The second object is a Byte object that represents the 
	 *		decoded ASN.1 Type. 
	 *
	 * @exception	AsnDecodingException	Thrown if an error occurs decoding
	 *			the buffer.
	 */
    public Object[] parseNull(byte[] buf, int startOffset) throws AsnDecodingException {
        Object[] hdrVals = parseHeader(buf, startOffset);
        if (((Integer) hdrVals[2]).intValue() != 0) throw new AsnDecodingException("Malformed ASN.1 Type");
        Object[] rVals = new Object[2];
        rVals[0] = hdrVals[0];
        rVals[1] = hdrVals[1];
        return rVals;
    }

    /**
	 *
	 * The buildString() method is used to encode an ASN.1 string value
	 * into the specified byte buffer.
	 *
	 * @param buf		The output buffer of encoded bytes.
	 * @param startOffset	The offset from the start of the buffer where the 
	 *			method should start writing the encoded data.
	 * @param asnType	The ASN.1 type to place in the buffer
	 * @param opaque	An array of bytes to encode into the string.
	 *
	 * @return	Returns the new offset for the next encoding routine.
	 *		If startOffset is subtracted from the return value
	 *		then the length of the encoded data can be determined.
	 *
	 * @exception	AsnEncodingException	Thrown if an error occurs encoding
	 *			the datatype.
	 * 
	 */
    public int buildString(byte[] buf, int startOffset, byte asnType, byte[] opaque) throws AsnEncodingException {
        int asnLength = opaque.length;
        startOffset = buildHeader(buf, startOffset, asnType, asnLength);
        if ((buf.length - startOffset) < opaque.length) throw new AsnEncodingException("Insufficent buffer length");
        try {
            copy(opaque, 0, buf, startOffset, opaque.length);
        } catch (ArrayIndexOutOfBoundsException err) {
            throw new AsnEncodingException("Buffer overflow error");
        }
        return startOffset + opaque.length;
    }

    /**
	 * 
	 * The parseString() method is used to decode an ASN.1 opaque string
	 * from the specified buffer.  
	 *
	 * @param buf		The input buffer
	 * @param startOffset	The offset to start decoding in the buffer
	 *
	 * @return	Returns an Object array that contains the new offset and
	 *		ASN.1 type, and byte array. The first object is an 
	 *		Integer object and contains the new offset for the next 
	 *		object in buf. The second object is a Byte object that 
	 *		represents the decoded ASN.1 Type. The third object is 
	 *		an array of primitive bytes.
	 *
	 * @exception	AsnDecodingException	Thrown if an error occurs decoding
	 *			the buffer.
	 */
    public Object[] parseString(byte[] buf, int startOffset) throws AsnDecodingException {
        Object[] hdrVals = parseHeader(buf, startOffset);
        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = ((Byte) hdrVals[1]);
        int asnLength = ((Integer) hdrVals[2]).intValue();
        if ((buf.length - startOffset) < asnLength) throw new AsnDecodingException("Insufficent buffer length");
        byte[] opaque = new byte[asnLength];
        try {
            copy(buf, startOffset, opaque, 0, asnLength);
        } catch (ArrayIndexOutOfBoundsException err) {
            throw new AsnDecodingException("Buffer underflow exception");
        }
        Object[] rVals = new Object[3];
        rVals[0] = new Integer(startOffset + asnLength);
        rVals[1] = asnType;
        rVals[2] = opaque;
        return rVals;
    }

    /**
	 *
	 * The buildObjectId() method is used to encode an ASN.1 object id value
	 * into the specified byte buffer.
	 *
	 * @param buf		The output buffer of encoded bytes.
	 * @param startOffset	The offset from the start of the buffer where the 
	 *			method should start writing the encoded data.
	 * @param asnType	The ASN.1 type to place in the buffer
	 * @param oids		An array of integers to encode.
	 *
	 * @return	Returns the new offset for the next encoding routine.
	 *		If startOffset is subtracted from the return value
	 *		then the length of the encoded data can be determined.
	 *
	 * @exception	AsnEncodingException	Thrown if an error occurs encoding
	 *			the datatype.
	 * 
	 */
    public int buildObjectId(byte[] buf, int startOffset, byte asnType, int[] oids) throws AsnEncodingException {
        if ((buf.length - startOffset) < 1) throw new AsnEncodingException("Buffer overflow error");
        int[] toEncode = oids;
        int begin = startOffset;
        if (oids.length < 2) {
            toEncode = new int[2];
            toEncode[0] = 0;
            toEncode[1] = 0;
        }
        if (toEncode[0] < 0 || toEncode[0] > 2) throw new AsnEncodingException("Invalid Object Identifier");
        if (toEncode[1] < 0 || toEncode[1] > 40) throw new AsnEncodingException("Invalid Object Identifier");
        buf[startOffset++] = (byte) (toEncode[0] * 40 + toEncode[1]);
        int oidNdx = 2;
        while (oidNdx < toEncode.length) {
            int oid = toEncode[oidNdx++];
            if (oid < 127) {
                if ((buf.length - startOffset) < 1) throw new AsnEncodingException("Buffer overflow error");
                buf[startOffset++] = (byte) oid;
            } else {
                int mask = 0, bits = 0;
                int tmask = 0, tbits = 0;
                tmask = 0x7f;
                tbits = 0;
                while (tmask != 0) {
                    if ((oid & tmask) != 0) {
                        mask = tmask;
                        bits = tbits;
                    }
                    tmask <<= 7;
                    tbits += 7;
                }
                while (mask != 0x7f) {
                    if ((buf.length - startOffset) < 1) throw new AsnEncodingException("Buffer overflow error");
                    buf[startOffset++] = (byte) (((oid & mask) >>> bits) | HIGH_BIT);
                    mask = (mask >>> 7);
                    bits -= 7;
                    if (mask == 0x01e00000) mask = 0x0fe00000;
                }
                if ((buf.length - startOffset) < 1) throw new AsnEncodingException("Insufficent buffer space");
                buf[startOffset++] = (byte) (oid & mask);
            }
        }
        int pivot = startOffset;
        int asnLength = pivot - begin;
        int end = buildHeader(buf, pivot, asnType, asnLength);
        try {
            rotate(buf, begin, pivot, end);
        } catch (ArrayIndexOutOfBoundsException err) {
            throw new AsnEncodingException("Insufficent buffer space");
        }
        return end;
    }

    /**
	 * 
	 * The parseObjectId() method is used to decode an ASN.1 Object Identifer
	 * from the specified buffer. 
	 *
	 * @param buf		The input buffer
	 * @param startOffset	The offset to start decoding in the buffer
	 *
	 * @return	Returns an Object array that contains the new offset and
	 *		ASN.1 type, and ObjectId array. The first object is an 
	 *		Integer object and contains the new offset for the next 
	 *		object in buf. The second object is a Byte object that 
	 *		represents the decoded ASN.1 Type. The third object is 
	 *		an array of primitive integers.
	 *
	 * @exception	AsnDecodingException	Thrown if an error occurs decoding
	 *			the buffer.
	 */
    public Object[] parseObjectId(byte[] buf, int startOffset) throws AsnDecodingException {
        Object[] hdrVals = parseHeader(buf, startOffset);
        startOffset = ((Integer) hdrVals[0]).intValue();
        Byte asnType = (Byte) hdrVals[1];
        int asnLength = ((Integer) hdrVals[2]).intValue();
        if ((buf.length - startOffset) < asnLength) throw new AsnDecodingException("Buffer underflow error");
        if (asnLength == 0) {
            int[] ids = new int[2];
            ids[0] = ids[1] = 0;
            Object[] rVals = new Object[3];
            rVals[0] = new Integer(startOffset);
            rVals[1] = asnType;
            rVals[2] = ids;
            return rVals;
        }
        int idsOff = 0;
        int[] ids = new int[asnLength + 1];
        {
            --asnLength;
            int oid = byteToInt(buf[startOffset++]);
            ids[idsOff++] = oid / 40;
            ids[idsOff++] = oid % 40;
        }
        while (asnLength > 0) {
            int oid = 0;
            boolean done = false;
            do {
                --asnLength;
                byte b = buf[startOffset++];
                oid = (oid << 7) | (int) (b & 0x7f);
                if ((b & HIGH_BIT) == 0) done = true;
            } while (!done);
            ids[idsOff++] = oid;
        }
        int[] retOids;
        if (idsOff == ids.length) {
            retOids = ids;
        } else {
            retOids = new int[idsOff];
            copy(ids, 0, retOids, 0, idsOff);
        }
        Object[] rVals = new Object[3];
        rVals[0] = new Integer(startOffset);
        rVals[1] = asnType;
        rVals[2] = retOids;
        return rVals;
    }
}
