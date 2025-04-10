package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlHexBinary;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.io.UnsupportedEncodingException;

public abstract class JavaHexBinaryHolder extends XmlObjectBase {

    public SchemaType schemaType() {
        return BuiltinSchemaTypeSystem.ST_HEX_BINARY;
    }

    protected byte[] _value;

    protected String compute_text(NamespaceManager nsm) {
        return new String(HexBin.encode(_value));
    }

    protected void set_text(String s) {
        _hashcached = false;
        if (_validateOnSet()) _value = validateLexical(s, schemaType(), _voorVc); else _value = lex(s, _voorVc);
    }

    protected void set_nil() {
        _hashcached = false;
        _value = null;
    }

    public static byte[] lex(String v, ValidationContext context) {
        byte[] vBytes = null;
        try {
            vBytes = v.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
        }
        byte[] bytes = HexBin.decode(vBytes);
        if (bytes == null) {
            context.invalid(XmlErrorCodes.HEXBINARY, new Object[] { "not encoded properly" });
        }
        return bytes;
    }

    public static byte[] validateLexical(String v, SchemaType sType, ValidationContext context) {
        byte[] bytes = lex(v, context);
        if (bytes == null) return null;
        if (!sType.matchPatternFacet(v)) {
            context.invalid("Hex encoded data does not match pattern for " + QNameHelper.readable(sType));
            return null;
        }
        return bytes;
    }

    public byte[] getByteArrayValue() {
        check_dated();
        if (_value == null) return null;
        byte[] result = new byte[_value.length];
        System.arraycopy(_value, 0, result, 0, _value.length);
        return result;
    }

    protected void set_ByteArray(byte[] ba) {
        _hashcached = false;
        _value = new byte[ba.length];
        System.arraycopy(ba, 0, _value, 0, ba.length);
    }

    protected boolean equal_to(XmlObject i) {
        byte[] ival = ((XmlHexBinary) i).getByteArrayValue();
        return Arrays.equals(_value, ival);
    }

    protected boolean _hashcached = false;

    protected int hashcode = 0;

    protected static MessageDigest md5;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot find MD5 hash Algorithm");
        }
    }

    protected int value_hash_code() {
        if (_hashcached) return hashcode;
        _hashcached = true;
        if (_value == null) return hashcode = 0;
        byte[] res = md5.digest(_value);
        return hashcode = res[0] << 24 + res[1] << 16 + res[2] << 8 + res[3];
    }
}
