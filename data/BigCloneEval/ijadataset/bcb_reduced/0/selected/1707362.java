package antiquity.util;

import ostore.util.ByteUtils;
import ostore.util.CountBuffer;
import ostore.util.InputBuffer;
import ostore.util.OutputBuffer;
import bamboo.util.XdrByteBufferEncodingStream;
import bamboo.util.XdrByteBufferDecodingStream;
import antiquity.util.XdrOutputBufferEncodingStream;
import bamboo.util.XdrInputBufferDecodingStream;
import bamboo.util.XdrClone;
import java.nio.ByteBuffer;
import org.acplt.oncrpc.XdrAble;
import org.acplt.oncrpc.XdrVoid;
import org.acplt.oncrpc.XdrDecodingStream;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import org.apache.log4j.Logger;

/**
 * Common methods to {@link #serialize}, {@link #deserialize}, {@link #clone}
 * and {@link #toString print} classes of type 
 * {@link org.acplt.oncrpc.XdrAble}.
 *
 * @author Hakim Weatherspoon
 * @version $Id: XdrUtils.java,v 1.3 2007/09/04 22:09:55 hweather Exp $
 **/
public class XdrUtils {

    private static Logger logger = Logger.getLogger(XdrUtils.class.getName());

    /** number of bytes to print for {@link #toString} methods. */
    public static int num_to_print = 4;

    /**
   * <code>serialize</code> an {@link org.acplt.oncrpc.XdrAble} class.
   * @param xdr {@link org.acplt.oncrpc.XdrAble} class to serialize.
   * @return {@link org.acplt.oncrpc.XdrAble} serialized into a byte array. */
    public static byte[] serialize(XdrAble xdr) {
        CountBuffer countBuffer = new CountBuffer();
        serialize(countBuffer, xdr);
        return serialize(countBuffer.size(), xdr);
    }

    /**
   * <code>serialize</code> an {@link org.acplt.oncrpc.XdrAble} class.
   * @param size Maximum number of bytes after serialization of 
   *              {@link org.acplt.oncrpc.XdrAble xdr} parameter. 
   * @param xdr {@link org.acplt.oncrpc.XdrAble} class to serialize.
   * @return {@link org.acplt.oncrpc.XdrAble} serialized into a byte array. */
    public static byte[] serialize(int size, XdrAble xdr) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        serialize(buffer, xdr);
        return buffer.array();
    }

    /**
   * <code>serialize</code> an {@link org.acplt.oncrpc.XdrAble} class.
   * @param buffer {@link java.nio.ByteBuffer} where 
   *               {@link org.acplt.oncrpc.XdrAble xdr} parameter should be
   *               serialized into. 
   * @param xdr {@link org.acplt.oncrpc.XdrAble} class to serialize. */
    public static void serialize(ByteBuffer buffer, XdrAble xdr) {
        XdrByteBufferEncodingStream es = new XdrByteBufferEncodingStream(buffer);
        try {
            xdr.xdrEncode(es);
        } catch (Exception e) {
            assert false : e;
        }
    }

    /**
   * <code>serialize</code> an {@link org.acplt.oncrpc.XdrAble} class.
   * @param buffer {@link ostore.util.OutputBuffer} where 
   *               {@link org.acplt.oncrpc.XdrAble xdr} parameter should be
   *               serialized into. 
   * @param xdr {@link org.acplt.oncrpc.XdrAble} class to serialize. */
    public static void serialize(OutputBuffer buffer, XdrAble xdr) {
        XdrOutputBufferEncodingStream es = new XdrOutputBufferEncodingStream(buffer);
        try {
            xdr.xdrEncode(es);
        } catch (Exception e) {
            assert false : e + " xdr=" + toString(xdr);
        }
    }

    /**
   * <code>deserialize</code> a {@link java.nio.ByteBuffer} into an
   * object of type {@link org.acplt.oncrpc.XdrAble}.
   * @param buffer {@link java.nio.ByteBuffer} containing serialized form of an
   *                object of type {@link org.acplt.oncrpc.XdrAble}.
   * @param clazz {@link java.lang.Class} of type 
   *                {@link org.acplt.oncrpc.XdrAble} used to create a new 
   *                instance.
   * @return Deserialized instance of an object of type 
   *           {@link org.acplt.oncrpc.XdrAble}. */
    public static <T extends XdrAble> T deserialize(ByteBuffer buffer, Class<T> clazz) {
        XdrByteBufferDecodingStream ds = new XdrByteBufferDecodingStream(buffer);
        return deserialize(ds, clazz);
    }

    /**
   * <code>deserialize</code> a {@link ostore.util.InputBuffer} into an
   * object of type {@link org.acplt.oncrpc.XdrAble}.
   * @param buffer {@link ostore.util.InputBuffer} containing serialized form 
   *                of an object of type {@link org.acplt.oncrpc.XdrAble}.
   * @param clazz {@link java.lang.Class} of type 
   *                {@link org.acplt.oncrpc.XdrAble} used to create a new
   *                instance.
   * @return Deserialized instance of an object of type 
   *           {@link org.acplt.oncrpc.XdrAble} class.*/
    public static <T extends XdrAble> T deserialize(InputBuffer buffer, Class<T> clazz) {
        int len = (Integer.MAX_VALUE >> 2) << 2;
        XdrInputBufferDecodingStream ds = new XdrInputBufferDecodingStream(buffer, len);
        return deserialize(ds, clazz);
    }

    /**
   * <code>deserialize</code> a {@link org.acplt.oncrpc.XdrDecodingStream} 
   * into an object of type {@link org.acplt.oncrpc.XdrAble}.
   * @param buffer {@link org.acplt.oncrpc.XdrDecodingStream} containing 
   *                serialized form of an object of type 
   *                 {@link org.acplt.oncrpc.XdrAble}.
   * @param clazz {@link java.lang.Class} of type 
   *                {@link org.acplt.oncrpc.XdrAble} used to create a new
   *                instance.
   * @return Deserialized instance of an object of type 
   *           {@link org.acplt.oncrpc.XdrAble} class.*/
    public static <T extends XdrAble> T deserialize(XdrDecodingStream ds, Class<T> clazz) {
        assert ds != null : "ds is null";
        assert clazz != null : "clazz is null";
        T xdrAble = null;
        try {
            ds.beginDecoding();
            xdrAble = clazz.getConstructor(new Class[] { XdrDecodingStream.class }).newInstance(new Object[] { ds });
        } catch (NoSuchMethodException nsme) {
            try {
                xdrAble = clazz.newInstance();
                xdrAble.xdrDecode(ds);
            } catch (Exception e) {
                assert false : " error while deserializing primitive " + clazz.getName() + ". " + e;
            }
        } catch (Exception e) {
            assert false : " error while deserializing " + clazz.getName() + ". " + e;
        }
        return xdrAble;
    }

    /**
   * <code>clone</code> {@link org.acplt.oncrpc.XdrAble} object by 
   * serializing and deserializing it.
   *
   * @param xdr {@link org.acplt.oncrpc.XdrAble} object to clone.
   * @return clone of {@link org.acplt.oncrpc.XdrAble} object. */
    public static <T extends XdrAble> T clone(T xdr) {
        CountBuffer countBuffer = new CountBuffer();
        serialize(countBuffer, xdr);
        ByteBuffer buffer = ByteBuffer.allocate(countBuffer.size() + ByteUtils.SIZE_INT);
        return (T) XdrClone.xdr_clone(xdr, buffer);
    }

    /**
   * <code>clone</code> {@link org.acplt.oncrpc.XdrAble} objects by 
   * serializing and deserializing them; the {@link java.nio.ByteBuffer buffer}
   * is cleared before serialization and not cleared after deserialization,
   * so the size of the object can be found by calling buf.position () after
   * a call to <code>clone</code>. 
   *
   * @param buffer {@link java.nio.ByteBuffer} used for cloning
   *                 {@link org.acplt.oncrpc.XdrAble} object.
   * @param xdr    {@link org.acplt.oncrpc.XdrAble} object to clone.
   * @return clone of {@link org.acplt.oncrpc.XdrAble} object. */
    public static XdrAble clone(ByteBuffer buffer, XdrAble xdr) {
        return XdrClone.xdr_clone(xdr, buffer);
    }

    /**
   * <code>equals</code> checks an array {@link org.acplt.oncrpc.XdrAble a} 
   * for equality with array {@link org.acplt.oncrpc.XdrAble b}.
   * Note it is possible for {@link org.acplt.oncrpc.XdrAble a} and
   * {@link org.acplt.oncrpc.XdrAble b} to be equal even though they
   * are from different packages.  This method checks that the field
   * values are equal.
   * (i.e. the byte format and layout are exactly the same for both objects.)
   *
   * For instance, <code>equals</code> would return <code>true</code> for  
   * comparing {@link antiquity.gw.api.gw_signed_certificate} to a 
   * {@link antiquity.ss.api.ss_signed_certificate} if all the field
   * values are equal.  These two class objects are exactly the same except 
   * for the package.
   *
   * @param a  {@link org.acplt.oncrpc.XdrAble} object to compare.
   * @param b  other {@link org.acplt.oncrpc.XdrAble} object to compare.
   *
   * @return  <code>true</code> if and only if all field values for both
   *          objects are equal. */
    public static boolean equals(XdrAble[] a, XdrAble[] b) {
        if (a == b || (a == null && b == null)) return true;
        if ((a != null && b == null) || (a == null && b != null)) return false;
        assert a != null && b != null;
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (!equals(a[i], b[i])) return false;
        }
        return true;
    }

    /**
   * <code>equals</code> checks an object {@link org.acplt.oncrpc.XdrAble a} 
   * for equality with an {@link org.acplt.oncrpc.XdrAble b}.
   * Note it is possible for {@link org.acplt.oncrpc.XdrAble a} and
   * {@link org.acplt.oncrpc.XdrAble b} to be equal even though they
   * are from different packages.  This method checks that the field
   * values are equal.
   * (i.e. the byte format and layout are exactly the same for both objects.)
   *
   * For instance, <code>equals</code> would return <code>true</code> for  
   * comparing {@link antiquity.gw.api.gw_signed_certificate} to a 
   * {@link antiquity.ss.api.ss_signed_certificate} if all the field
   * values are equal.  These two class objects are exactly the same except 
   * for the package.
   *
   * @param a  {@link org.acplt.oncrpc.XdrAble} object to compare.
   * @param b  other {@link org.acplt.oncrpc.XdrAble} object to compare.
   *
   * @return  <code>true</code> if and only if all field values for both
   *          objects are equal. */
    public static boolean equals(XdrAble a, XdrAble b) {
        final String method_tag = XdrUtils.class.getName() + ".equals";
        if (a == b || (a == null && b == null)) return true;
        if ((a != null && b == null) || (a == null && b != null)) return false;
        assert a != null && b != null;
        if ((a instanceof XdrVoid) && (b instanceof XdrVoid)) return true;
        if (((a instanceof XdrVoid) && !(b instanceof XdrVoid)) || (!(a instanceof XdrVoid) && (b instanceof XdrVoid))) return false;
        assert !(a instanceof XdrVoid) && !(b instanceof XdrVoid);
        Field[] fields = a.getClass().getFields();
        for (Field aField : fields) {
            Object aValue = null;
            try {
                aValue = aField.get(a);
            } catch (Exception e) {
                assert false : "why could we not get value of field " + aField;
            }
            Field bField = null;
            try {
                bField = b.getClass().getField(aField.getName());
            } catch (Exception e) {
                if (logger.isInfoEnabled()) logger.info(method_tag + ": No aField=" + aField + " in class " + b + ". aValue=" + aValue + " for a class=" + a);
                return false;
            }
            Object bValue = null;
            try {
                bValue = bField.get(b);
            } catch (Exception e) {
                return false;
            }
            if (aValue == null && bValue == null) {
                continue;
            } else if ((aValue == null && bValue != null) || (aValue != null && bValue == null)) {
                return false;
            }
            assert aValue != null && bValue != null;
            if ((a instanceof XdrVoid) && (b instanceof XdrVoid)) {
                continue;
            } else if (((aValue instanceof XdrVoid) && !(bValue instanceof XdrVoid)) || (!(aValue instanceof XdrVoid) && (bValue instanceof XdrVoid))) {
                return false;
            }
            assert !(a instanceof XdrVoid) && !(b instanceof XdrVoid);
            if (aValue instanceof XdrAble) {
                if (!(bValue instanceof XdrAble) || (bValue instanceof XdrVoid)) return false;
                XdrAble aXdrValue = (XdrAble) aValue;
                XdrAble bXdrValue = (XdrAble) bValue;
                if (!equals(aXdrValue, bXdrValue)) return false;
            } else if (aValue instanceof XdrAble[]) {
                if (!(bValue instanceof XdrAble[])) return false;
                XdrAble[] aXdrValueArray = (XdrAble[]) aValue;
                XdrAble[] bXdrValueArray = (XdrAble[]) bValue;
                if (!equals(aXdrValueArray, bXdrValueArray)) return false;
            } else if (aValue instanceof byte[]) {
                if (!(bValue instanceof byte[])) return false;
                byte[] aByteArray = (byte[]) aValue;
                byte[] bByteArray = (byte[]) bValue;
                if (!ByteUtils.equals(aByteArray, bByteArray)) return false;
            } else {
                try {
                    if (!aValue.equals(bValue)) return false;
                } catch (Exception e) {
                    if (logger.isInfoEnabled()) logger.info(method_tag + ": received error when performing aValue=" + aValue + " equals bValue=" + bValue + ". Error  " + e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
   * <code>toString</code> returns a {@link java.lang.String}
   * representation of the array of objects of type 
   * {@link org.acplt.oncrpc.XdrAble}. In general, the
   * <code>toString</code> method returns a string that "textually
   * represents" this {@link org.acplt.oncrpc.XdrAble} object array.
   * The result should be a concise but informative representation
   * that is easy for a person to read.
   *
   * @param xdr {@link org.acplt.oncrpc.XdrAble} object array to convert to a 
   *            {@link java.lang.String} representation.   
   * @return {@link java.lang.String} representation of the object of type
   *         {@link org.acplt.oncrpc.XdrAble}. */
    public static String toString(XdrAble[] xdr) {
        if (xdr == null) return new String("null");
        String str = new String();
        for (int i = 0; xdr != null && i < xdr.length; i++) str += "" + (i == 0 ? "" : ", " + i + ":") + toString(xdr[i]);
        return str;
    }

    /**
   * <code>toString</code> returns a {@link java.lang.String} representation 
   * of the object of type {@link org.acplt.oncrpc.XdrAble}. In general, 
   * the <code>toString</code> method returns a string that "textually
   * represents" this {@link org.acplt.oncrpc.XdrAble} object. The
   * result should be a concise but informative representation that is
   * easy for a person to read.
   *
   * @param xdr {@link org.acplt.oncrpc.XdrAble} object to convert to a 
   *            {@link java.lang.String} representation.   
   * @return {@link java.lang.String} representation of the object of type
   *         {@link org.acplt.oncrpc.XdrAble}. */
    public static String toString(XdrAble xdr) {
        if (xdr == null) return new String("null");
        String str = new String("(" + xdr.getClass().getSimpleName());
        if (xdr instanceof XdrVoid) return str + ")";
        Field[] fields = xdr.getClass().getFields();
        for (Field field : fields) {
            String tmp_str = null;
            try {
                tmp_str = " " + (field.getName().equals("value") ? "" : field.getName() + "=");
            } catch (Exception e) {
                break;
            }
            Object obj = null;
            try {
                obj = field.get(xdr);
            } catch (Exception e) {
                str += tmp_str + "null";
                break;
            }
            if (obj == null) {
                continue;
            } else if (obj.getClass().getName().equals(xdr.getClass().getName())) {
                continue;
            }
            str += tmp_str;
            if (obj instanceof XdrVoid) {
                str += "" + obj;
            } else if (obj instanceof XdrAble) {
                str += "" + toString((XdrAble) obj);
            } else if (obj instanceof XdrAble[]) {
                XdrAble[] xdrArray = (XdrAble[]) obj;
                str += "{";
                for (int i = 0; i < xdrArray.length; i++) str += "" + (i == 0 ? "" : ", ") + i + ":" + toString(xdrArray[i]);
                str += "}";
            } else if (obj instanceof byte[]) {
                byte[] bytes = (byte[]) obj;
                if (bytes.length == 4) {
                    for (int i = 0; i < bytes.length; i++) str += "" + (i == 0 ? "" : ".") + ((int) bytes[i] & 0xff);
                } else if (bytes.length >= num_to_print) str += "0x" + ByteUtils.print_bytes(bytes, 0, num_to_print); else str += "0x" + ByteUtils.print_bytes(bytes, 0, bytes.length);
            } else {
                str += "" + obj;
            }
        }
        str += ")";
        return str;
    }
}
