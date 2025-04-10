package ch.comtools.jsch;

import java.io.*;

class IdentityFile implements Identity {

    String identity;

    byte[] key;

    byte[] iv;

    private JSch jsch;

    private HASH hash;

    private byte[] encoded_data;

    private Cipher cipher;

    private byte[] P_array;

    private byte[] Q_array;

    private byte[] G_array;

    private byte[] pub_array;

    private byte[] prv_array;

    private byte[] n_array;

    private byte[] e_array;

    private byte[] d_array;

    private String algname = "ssh-rsa";

    private static final int ERROR = 0;

    private static final int RSA = 1;

    private static final int DSS = 2;

    private static final int UNKNOWN = 3;

    private static final int OPENSSH = 0;

    private static final int FSECURE = 1;

    private static final int PUTTY = 2;

    private int type = ERROR;

    private int keytype = OPENSSH;

    private byte[] publickeyblob = null;

    private boolean encrypted = true;

    static IdentityFile newInstance(String prvfile, String pubfile, JSch jsch) throws JSchException {
        byte[] prvkey = null;
        byte[] pubkey = null;
        File file = null;
        FileInputStream fis = null;
        try {
            file = new File(prvfile);
            fis = new FileInputStream(prvfile);
            prvkey = new byte[(int) (file.length())];
            int len = 0;
            while (true) {
                int i = fis.read(prvkey, len, prvkey.length - len);
                if (i <= 0) break;
                len += i;
            }
            fis.close();
        } catch (Exception e) {
            try {
                if (fis != null) fis.close();
            } catch (Exception ee) {
            }
            if (e instanceof Throwable) throw new JSchException(e.toString(), (Throwable) e);
            throw new JSchException(e.toString());
        }
        String _pubfile = pubfile;
        if (pubfile == null) {
            _pubfile = prvfile + ".pub";
        }
        try {
            file = new File(_pubfile);
            fis = new FileInputStream(_pubfile);
            pubkey = new byte[(int) (file.length())];
            int len = 0;
            while (true) {
                int i = fis.read(pubkey, len, pubkey.length - len);
                if (i <= 0) break;
                len += i;
            }
            fis.close();
        } catch (Exception e) {
            try {
                if (fis != null) fis.close();
            } catch (Exception ee) {
            }
            if (pubfile != null) {
                if (e instanceof Throwable) throw new JSchException(e.toString(), (Throwable) e);
                throw new JSchException(e.toString());
            }
        }
        return newInstance(prvfile, prvkey, pubkey, jsch);
    }

    static IdentityFile newInstance(String name, byte[] prvkey, byte[] pubkey, JSch jsch) throws JSchException {
        try {
            return new IdentityFile(name, prvkey, pubkey, jsch);
        } finally {
            Util.bzero(prvkey);
        }
    }

    private IdentityFile(String name, byte[] prvkey, byte[] pubkey, JSch jsch) throws JSchException {
        this.identity = name;
        this.jsch = jsch;
        try {
            Class c;
            c = Class.forName((String) jsch.getConfig("3des-cbc"));
            cipher = (Cipher) (c.newInstance());
            key = new byte[cipher.getBlockSize()];
            iv = new byte[cipher.getIVSize()];
            c = Class.forName((String) jsch.getConfig("md5"));
            hash = (HASH) (c.newInstance());
            hash.init();
            byte[] buf = prvkey;
            int len = buf.length;
            int i = 0;
            while (i < len) {
                if (buf[i] == 'B' && buf[i + 1] == 'E' && buf[i + 2] == 'G' && buf[i + 3] == 'I') {
                    i += 6;
                    if (buf[i] == 'D' && buf[i + 1] == 'S' && buf[i + 2] == 'A') {
                        type = DSS;
                    } else if (buf[i] == 'R' && buf[i + 1] == 'S' && buf[i + 2] == 'A') {
                        type = RSA;
                    } else if (buf[i] == 'S' && buf[i + 1] == 'S' && buf[i + 2] == 'H') {
                        type = UNKNOWN;
                        keytype = FSECURE;
                    } else {
                        throw new JSchException("invalid privatekey: " + identity);
                    }
                    i += 3;
                    continue;
                }
                if (buf[i] == 'C' && buf[i + 1] == 'B' && buf[i + 2] == 'C' && buf[i + 3] == ',') {
                    i += 4;
                    for (int ii = 0; ii < iv.length; ii++) {
                        iv[ii] = (byte) (((a2b(buf[i++]) << 4) & 0xf0) + (a2b(buf[i++]) & 0xf));
                    }
                    continue;
                }
                if (buf[i] == 0x0d && i + 1 < buf.length && buf[i + 1] == 0x0a) {
                    i++;
                    continue;
                }
                if (buf[i] == 0x0a && i + 1 < buf.length) {
                    if (buf[i + 1] == 0x0a) {
                        i += 2;
                        break;
                    }
                    if (buf[i + 1] == 0x0d && i + 2 < buf.length && buf[i + 2] == 0x0a) {
                        i += 3;
                        break;
                    }
                    boolean inheader = false;
                    for (int j = i + 1; j < buf.length; j++) {
                        if (buf[j] == 0x0a) break;
                        if (buf[j] == ':') {
                            inheader = true;
                            break;
                        }
                    }
                    if (!inheader) {
                        i++;
                        encrypted = false;
                        break;
                    }
                }
                i++;
            }
            if (type == ERROR) {
                throw new JSchException("invalid privatekey: " + identity);
            }
            int start = i;
            while (i < len) {
                if (buf[i] == 0x0a) {
                    boolean xd = (buf[i - 1] == 0x0d);
                    System.arraycopy(buf, i + 1, buf, i - (xd ? 1 : 0), len - i - 1 - (xd ? 1 : 0));
                    if (xd) len--;
                    len--;
                    continue;
                }
                if (buf[i] == '-') {
                    break;
                }
                i++;
            }
            encoded_data = Util.fromBase64(buf, start, i - start);
            if (encoded_data.length > 4 && encoded_data[0] == (byte) 0x3f && encoded_data[1] == (byte) 0x6f && encoded_data[2] == (byte) 0xf9 && encoded_data[3] == (byte) 0xeb) {
                Buffer _buf = new Buffer(encoded_data);
                _buf.getInt();
                _buf.getInt();
                byte[] _type = _buf.getString();
                byte[] _cipher = _buf.getString();
                String cipher = new String(_cipher);
                if (cipher.equals("3des-cbc")) {
                    _buf.getInt();
                    byte[] foo = new byte[encoded_data.length - _buf.getOffSet()];
                    _buf.getByte(foo);
                    encoded_data = foo;
                    encrypted = true;
                    throw new JSchException("unknown privatekey format: " + identity);
                } else if (cipher.equals("none")) {
                    _buf.getInt();
                    encrypted = false;
                    byte[] foo = new byte[encoded_data.length - _buf.getOffSet()];
                    _buf.getByte(foo);
                    encoded_data = foo;
                }
            }
            if (pubkey == null) {
                return;
            }
            buf = pubkey;
            len = buf.length;
            if (buf.length > 4 && buf[0] == '-' && buf[1] == '-' && buf[2] == '-' && buf[3] == '-') {
                i = 0;
                do {
                    i++;
                } while (len > i && buf[i] != 0x0a);
                if (len <= i) return;
                while (i < len) {
                    if (buf[i] == 0x0a) {
                        boolean inheader = false;
                        for (int j = i + 1; j < len; j++) {
                            if (buf[j] == 0x0a) break;
                            if (buf[j] == ':') {
                                inheader = true;
                                break;
                            }
                        }
                        if (!inheader) {
                            i++;
                            break;
                        }
                    }
                    i++;
                }
                if (len <= i) return;
                start = i;
                while (i < len) {
                    if (buf[i] == 0x0a) {
                        System.arraycopy(buf, i + 1, buf, i, len - i - 1);
                        len--;
                        continue;
                    }
                    if (buf[i] == '-') {
                        break;
                    }
                    i++;
                }
                publickeyblob = Util.fromBase64(buf, start, i - start);
                if (type == UNKNOWN) {
                    if (publickeyblob[8] == 'd') {
                        type = DSS;
                    } else if (publickeyblob[8] == 'r') {
                        type = RSA;
                    }
                }
            } else {
                if (buf[0] != 's' || buf[1] != 's' || buf[2] != 'h' || buf[3] != '-') return;
                i = 0;
                while (i < len) {
                    if (buf[i] == ' ') break;
                    i++;
                }
                i++;
                if (i >= len) return;
                start = i;
                while (i < len) {
                    if (buf[i] == ' ' || buf[i] == '\n') break;
                    i++;
                }
                publickeyblob = Util.fromBase64(buf, start, i - start);
            }
        } catch (Exception e) {
            if (e instanceof JSchException) throw (JSchException) e;
            if (e instanceof Throwable) throw new JSchException(e.toString(), (Throwable) e);
            throw new JSchException(e.toString());
        }
    }

    public String getAlgName() {
        if (type == RSA) return "ssh-rsa";
        return "ssh-dss";
    }

    public boolean setPassphrase(byte[] _passphrase) throws JSchException {
        try {
            if (encrypted) {
                if (_passphrase == null) return false;
                byte[] passphrase = _passphrase;
                int hsize = hash.getBlockSize();
                byte[] hn = new byte[key.length / hsize * hsize + (key.length % hsize == 0 ? 0 : hsize)];
                byte[] tmp = null;
                if (keytype == OPENSSH) {
                    for (int index = 0; index + hsize <= hn.length; ) {
                        if (tmp != null) {
                            hash.update(tmp, 0, tmp.length);
                        }
                        hash.update(passphrase, 0, passphrase.length);
                        hash.update(iv, 0, iv.length);
                        tmp = hash.digest();
                        System.arraycopy(tmp, 0, hn, index, tmp.length);
                        index += tmp.length;
                    }
                    System.arraycopy(hn, 0, key, 0, key.length);
                } else if (keytype == FSECURE) {
                    for (int index = 0; index + hsize <= hn.length; ) {
                        if (tmp != null) {
                            hash.update(tmp, 0, tmp.length);
                        }
                        hash.update(passphrase, 0, passphrase.length);
                        tmp = hash.digest();
                        System.arraycopy(tmp, 0, hn, index, tmp.length);
                        index += tmp.length;
                    }
                    System.arraycopy(hn, 0, key, 0, key.length);
                }
                Util.bzero(passphrase);
            }
            if (decrypt()) {
                encrypted = false;
                return true;
            }
            P_array = Q_array = G_array = pub_array = prv_array = null;
            return false;
        } catch (Exception e) {
            if (e instanceof JSchException) throw (JSchException) e;
            if (e instanceof Throwable) throw new JSchException(e.toString(), (Throwable) e);
            throw new JSchException(e.toString());
        }
    }

    public byte[] getPublicKeyBlob() {
        if (publickeyblob != null) return publickeyblob;
        if (type == RSA) return getPublicKeyBlob_rsa();
        return getPublicKeyBlob_dss();
    }

    byte[] getPublicKeyBlob_rsa() {
        if (e_array == null) return null;
        Buffer buf = new Buffer("ssh-rsa".length() + 4 + e_array.length + 4 + n_array.length + 4);
        buf.putString("ssh-rsa".getBytes());
        buf.putString(e_array);
        buf.putString(n_array);
        return buf.buffer;
    }

    byte[] getPublicKeyBlob_dss() {
        if (P_array == null) return null;
        Buffer buf = new Buffer("ssh-dss".length() + 4 + P_array.length + 4 + Q_array.length + 4 + G_array.length + 4 + pub_array.length + 4);
        buf.putString("ssh-dss".getBytes());
        buf.putString(P_array);
        buf.putString(Q_array);
        buf.putString(G_array);
        buf.putString(pub_array);
        return buf.buffer;
    }

    public byte[] getSignature(byte[] data) {
        if (type == RSA) return getSignature_rsa(data);
        return getSignature_dss(data);
    }

    byte[] getSignature_rsa(byte[] data) {
        try {
            Class c = Class.forName((String) jsch.getConfig("signature.rsa"));
            SignatureRSA rsa = (SignatureRSA) (c.newInstance());
            rsa.init();
            rsa.setPrvKey(d_array, n_array);
            rsa.update(data);
            byte[] sig = rsa.sign();
            Buffer buf = new Buffer("ssh-rsa".length() + 4 + sig.length + 4);
            buf.putString("ssh-rsa".getBytes());
            buf.putString(sig);
            return buf.buffer;
        } catch (Exception e) {
        }
        return null;
    }

    byte[] getSignature_dss(byte[] data) {
        try {
            Class c = Class.forName((String) jsch.getConfig("signature.dss"));
            SignatureDSA dsa = (SignatureDSA) (c.newInstance());
            dsa.init();
            dsa.setPrvKey(prv_array, P_array, Q_array, G_array);
            dsa.update(data);
            byte[] sig = dsa.sign();
            Buffer buf = new Buffer("ssh-dss".length() + 4 + sig.length + 4);
            buf.putString("ssh-dss".getBytes());
            buf.putString(sig);
            return buf.buffer;
        } catch (Exception e) {
        }
        return null;
    }

    public boolean decrypt() {
        if (type == RSA) return decrypt_rsa();
        return decrypt_dss();
    }

    boolean decrypt_rsa() {
        byte[] p_array;
        byte[] q_array;
        byte[] dmp1_array;
        byte[] dmq1_array;
        byte[] iqmp_array;
        try {
            byte[] plain;
            if (encrypted) {
                if (keytype == OPENSSH) {
                    cipher.init(Cipher.DECRYPT_MODE, key, iv);
                    plain = new byte[encoded_data.length];
                    cipher.update(encoded_data, 0, encoded_data.length, plain, 0);
                } else if (keytype == FSECURE) {
                    for (int i = 0; i < iv.length; i++) iv[i] = 0;
                    cipher.init(Cipher.DECRYPT_MODE, key, iv);
                    plain = new byte[encoded_data.length];
                    cipher.update(encoded_data, 0, encoded_data.length, plain, 0);
                } else {
                    return false;
                }
            } else {
                if (n_array != null) return true;
                plain = encoded_data;
            }
            if (keytype == FSECURE) {
                Buffer buf = new Buffer(plain);
                int foo = buf.getInt();
                if (plain.length != foo + 4) {
                    return false;
                }
                e_array = buf.getMPIntBits();
                d_array = buf.getMPIntBits();
                n_array = buf.getMPIntBits();
                byte[] u_array = buf.getMPIntBits();
                p_array = buf.getMPIntBits();
                q_array = buf.getMPIntBits();
                return true;
            }
            int index = 0;
            int length = 0;
            if (plain[index] != 0x30) return false;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            if (plain[index] != 0x02) return false;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            n_array = new byte[length];
            System.arraycopy(plain, index, n_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            e_array = new byte[length];
            System.arraycopy(plain, index, e_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            d_array = new byte[length];
            System.arraycopy(plain, index, d_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            p_array = new byte[length];
            System.arraycopy(plain, index, p_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            q_array = new byte[length];
            System.arraycopy(plain, index, q_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            dmp1_array = new byte[length];
            System.arraycopy(plain, index, dmp1_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            dmq1_array = new byte[length];
            System.arraycopy(plain, index, dmq1_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            iqmp_array = new byte[length];
            System.arraycopy(plain, index, iqmp_array, 0, length);
            index += length;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    boolean decrypt_dss() {
        try {
            byte[] plain;
            if (encrypted) {
                if (keytype == OPENSSH) {
                    cipher.init(Cipher.DECRYPT_MODE, key, iv);
                    plain = new byte[encoded_data.length];
                    cipher.update(encoded_data, 0, encoded_data.length, plain, 0);
                } else if (keytype == FSECURE) {
                    for (int i = 0; i < iv.length; i++) iv[i] = 0;
                    cipher.init(Cipher.DECRYPT_MODE, key, iv);
                    plain = new byte[encoded_data.length];
                    cipher.update(encoded_data, 0, encoded_data.length, plain, 0);
                } else {
                    return false;
                }
            } else {
                if (P_array != null) return true;
                plain = encoded_data;
            }
            if (keytype == FSECURE) {
                Buffer buf = new Buffer(plain);
                int foo = buf.getInt();
                if (plain.length != foo + 4) {
                    return false;
                }
                P_array = buf.getMPIntBits();
                G_array = buf.getMPIntBits();
                Q_array = buf.getMPIntBits();
                pub_array = buf.getMPIntBits();
                prv_array = buf.getMPIntBits();
                return true;
            }
            int index = 0;
            int length = 0;
            if (plain[index] != 0x30) return false;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            if (plain[index] != 0x02) return false;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            P_array = new byte[length];
            System.arraycopy(plain, index, P_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            Q_array = new byte[length];
            System.arraycopy(plain, index, Q_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            G_array = new byte[length];
            System.arraycopy(plain, index, G_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            pub_array = new byte[length];
            System.arraycopy(plain, index, pub_array, 0, length);
            index += length;
            index++;
            length = plain[index++] & 0xff;
            if ((length & 0x80) != 0) {
                int foo = length & 0x7f;
                length = 0;
                while (foo-- > 0) {
                    length = (length << 8) + (plain[index++] & 0xff);
                }
            }
            prv_array = new byte[length];
            System.arraycopy(plain, index, prv_array, 0, length);
            index += length;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public String getName() {
        return identity;
    }

    private byte a2b(byte c) {
        if ('0' <= c && c <= '9') return (byte) (c - '0');
        if ('a' <= c && c <= 'z') return (byte) (c - 'a' + 10);
        return (byte) (c - 'A' + 10);
    }

    public boolean equals(Object o) {
        if (!(o instanceof IdentityFile)) return super.equals(o);
        IdentityFile foo = (IdentityFile) o;
        return getName().equals(foo.getName());
    }

    public void clear() {
        Util.bzero(encoded_data);
        Util.bzero(prv_array);
        Util.bzero(d_array);
        Util.bzero(key);
        Util.bzero(iv);
    }

    public void finalize() {
        clear();
    }
}
