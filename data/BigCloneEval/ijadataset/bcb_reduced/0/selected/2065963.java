package jws.core.io;

import jws.util.*;
import jws.core.config.JwsConfiguration;
import jws.core.config.ConfigurationException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Deflater;
import java.util.Map;
import java.util.HashMap;

/**
 * Abstract base class for serializing of portable objects.<br></br><br></br>
 * {@link ObjectWriter} is can encrypt and/or compress the produced stream. Encryption
 * and compression features of {@link ObjectWriter} are controlled by module-wide properties
 * defined in the module configuration file (see {@link jws.core.config.JwsConfiguration}).
 * Specifically, if <code>jws.serialization.compress</code> property is set to <code>true</code>,
 * {@link ObjectWriter} tries to decompress the serialized object stream. Similarly, if
 * <code>jws.serialization.encrypt</code> property is set to <code>true</code>, it tries to
 * encrypt the stream with the password provided by <code>jws.serialization.encryptionPassword</code>
 * property.<br></br><br></br>
 * Encryption and compression may be either combined or used separately.
 * @see BinaryObjectWriter
 * @see ObjectReader
 * @see JwsConfiguration
 */
public abstract class ObjectWriter implements CryptConst {

    private ByteArrayOutputStream _baos = null;

    private Integer _def_level = null;

    private SecretKeySpec _key = null;

    private static final IvParameterSpec _iv = new IvParameterSpec(_salt);

    private Cipher _cipher = null;

    private OutputStream _outs = null;

    /**
     * Creates a new instance of {@link ObjectWriter}.
     * @param params Set of parameters to initialize this instance of {@link jws.core.io.ObjectWriter}
     * @throws SerializationException If this {@link ObjectWriter}
     * failed to initialize itself
     */
    protected ObjectWriter(Map<String, Object> params) throws SerializationException {
        int buffer_size = 1024 * 1024;
        try {
            buffer_size = (Integer) JwsConfiguration.getParameter("transport.bufferSize", 1024 * 1024);
        } catch (ConfigurationException ex) {
        }
        _baos = new ByteArrayOutputStream(buffer_size);
        _outs = _baos;
        boolean compress;
        boolean encrypt;
        try {
            compress = Boolean.TRUE.equals(getParam(params, "transport.writer.compress", "transport.compress", false));
            encrypt = Boolean.TRUE.equals(getParam(params, "transport.writer.encrypt", "transport.encrypt", false));
        } catch (ConfigurationException ex) {
            throw new SerializationException("Object writer is not properly configured", ex);
        }
        if (encrypt) {
            try {
                String password = (String) getParam(params, "transport.writer.encryptionPassword", "transport.encryptionPassword", null);
                if (password == null) {
                    throw new ConfigurationException("No password provided for encryption");
                }
                byte[] buff = UTF8.encode(SysUtils.encrypt(password, "MD5"));
                _key = new SecretKeySpec(buff, 0, 24, "DESede");
                _cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            } catch (Throwable th) {
                throw new SerializationException("Failed to initialize encryption stream", th);
            }
        }
        if (compress) {
            try {
                int level = (Integer) getParam(params, "transport.writer.compressionLevel", "transport.compressionLevel", 9);
                if ((level < 0) || (level > 9)) {
                    throw new ConfigurationException("Invalid compression level " + level + ". Should be within 0..9");
                }
                _def_level = level;
            } catch (Throwable th) {
                throw new SerializationException("Failed to initialize compression stream", th);
            }
        }
    }

    /**
     * Writes bytes from from given offset in the specified byte array into the underlying stream.
     * @param b Byte array to get data from
     * @param off Offset in <code>b</code> to get data at
     * @param len Maximim number of bytes to be written
     * @throws IOException If some IO error occurs
     */
    protected final void write(byte[] b, int off, int len) throws IOException {
        _outs.write(b, off, len);
    }

    /**
     * Writes bytes from the specified byte array into the underlying stream.<br></br><br></br>
     * Calling this method is essentially the same as calling.
     * <pre>
     * write(b, 0, b.length);
     * </pre>
     * @param b Byte array to write to the stream
     * @throws IOException If some IO error occurs
     */
    protected final void write(byte[] b) throws IOException {
        _outs.write(b);
    }

    /**
     * Writes a portable object to the underlying stream. Implementations must provide this
     * method by means of <code>write</code> routines defined in {@link ObjectWriter}.
     * @param obj Object instance to be serialized
     * @throws SerializationException If the object can not be serialized to the underlying stream
     */
    protected abstract void writeObject(Object obj) throws SerializationException;

    private byte[] doSerializeObject(Object obj) throws SerializationException {
        _baos.reset();
        _outs = _baos;
        writeObject(obj);
        if (_def_level != null) {
            try {
                byte[] data = _baos.toByteArray();
                ByteArrayOutputStream baos = new ByteArrayOutputStream(_baos.size());
                DeflaterOutputStream defs = new DeflaterOutputStream(baos, new Deflater(_def_level));
                defs.write(data, 0, data.length);
                defs.flush();
                defs.finish();
                defs.close();
                baos.flush();
                _baos = baos;
            } catch (Throwable th) {
                throw new SerializationException("Failed to compress serialized data", th);
            }
        }
        if ((_cipher != null) && (_key != null)) {
            try {
                byte[] data = _baos.toByteArray();
                ByteArrayOutputStream baos = new ByteArrayOutputStream(_baos.size());
                _cipher.init(Cipher.ENCRYPT_MODE, _key, _iv);
                CipherOutputStream cifs = new CipherOutputStream(baos, _cipher);
                cifs.write(data, 0, data.length);
                cifs.flush();
                cifs.close();
                _baos = baos;
            } catch (Throwable th) {
                throw new SerializationException("Failed to init cryptographic cipher", th);
            }
        }
        return _baos.toByteArray();
    }

    private static HashMap<String, Pool<ObjectWriter>> _pools = new HashMap<String, Pool<ObjectWriter>>();

    /**
	 * It's util method that serializes an object to byte array with specified object writer parameters.
	 * @param obj Object to be serialized
	 * @param parameters object wrieter parameters needed to initialize it
	 * @return Byte array contained serialized object data
	 * @throws SerializationException If specified object can not be serialized or object writer initialization failed
	 */
    @SuppressWarnings("unchecked")
    public static byte[] serializeObject(Object obj, Map<String, Object> parameters) throws SerializationException {
        final Map<String, Object> params;
        if (parameters != null) {
            params = parameters;
        } else {
            params = new HashMap<String, Object>();
        }
        String key = generateMapKey(params);
        Pool<ObjectWriter> pool = _pools.get(key);
        String writerClassName = null;
        if (pool == null) {
            try {
                writerClassName = (String) getParam(params, "transport.writer", "transport.writer", BinaryObjectWriter.class.getName());
                final Class<? extends ObjectWriter> cls = (Class<? extends ObjectWriter>) ReflectUtils.loadClass(writerClassName);
                pool = new Pool<ObjectWriter>(new IObjectFactory<ObjectWriter>() {

                    public ObjectWriter createObject() throws Exception {
                        return ReflectUtils.createInstance(cls, new Class[] { Map.class }, new Object[] { params });
                    }
                }, 10);
                _pools.put(key, pool);
            } catch (Throwable th) {
                throw new SerializationException("Invalid object writer class: " + writerClassName, th);
            }
        }
        ObjectWriter writer = null;
        try {
            writer = pool.getObject();
            return writer.doSerializeObject(obj);
        } catch (PoolException ex) {
            throw new SerializationException("Failed to obtain writer object from the pool", ex);
        } finally {
            if (writer != null) {
                try {
                    pool.releaseObject(writer);
                } catch (PoolException ex) {
                }
            }
        }
    }

    private static String generateMapKey(Map<String, Object> map) {
        String str = "";
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            str += entry.getKey() + (entry.getValue() == null ? "-" : "" + entry.getValue().hashCode());
        }
        return str;
    }

    protected static Object getParam(Map<String, Object> params, String name1, String name2, Object def) throws ConfigurationException {
        Object obj = params.get(name1);
        if (obj == null) {
            obj = params.get(name2);
            if (obj == null) {
                obj = JwsConfiguration.getParameter(name1);
                if (obj == null) {
                    obj = JwsConfiguration.getParameter(name2, def);
                }
            }
        }
        return obj;
    }
}
