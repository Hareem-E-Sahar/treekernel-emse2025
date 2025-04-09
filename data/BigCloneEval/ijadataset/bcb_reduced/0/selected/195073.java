package net.sourceforge.sdm.util;

import net.sourceforge.sdm.resources.*;
import net.sourceforge.sdm.util.SecureFileAccess;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.ResourceBundle;

public class RSA implements SecureFileAccess {

    static ResourceBundle res = ResourceFactory.getBundle();

    private String algorithm = res.getString("RSA");

    private byte[] salt = new byte[8];

    private int iterations = 20;

    private SecretKey key;

    private long fileLength;

    private File dataFile;

    private Cipher cipher;

    private Provider sunJce;

    private String filePasswd;

    private String keyPasswd;

    private String user;

    private String keyFile;

    public RSA() {
    }

    public void setFilePassPhrase(String Password) {
        filePasswd = Password;
    }

    public void setKeyPassPhrase(String Password) {
        keyPasswd = Password;
    }

    public void setUser(String userName) {
        user = userName;
    }

    public void setKeyStore(String fileName) {
        keyFile = fileName;
    }

    public boolean ChangeKeyPassPhrase(String oldPassword, String newPassword, String fileName) {
        FileInputStream fsKeysIn;
        FileOutputStream fsKeysOut;
        try {
            if ((keyFile == null) || (keyFile.length() == 0)) {
                fsKeysIn = null;
            } else {
                fsKeysIn = new FileInputStream(keyFile);
            }
            KeyStore ks = KeyStore.getInstance("JCEKS");
            ks.load(fsKeysIn, oldPassword.toCharArray());
            if (fsKeysIn != null) {
                fsKeysIn.close();
            }
            if ((keyFile == null) || (keyFile.length() == 0)) {
                fsKeysOut = new FileOutputStream("SDM.keystore");
            } else {
                fsKeysOut = new FileOutputStream(keyFile);
            }
            ks.store(fsKeysOut, newPassword.toCharArray());
            fsKeysOut.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean WriteFile(java.io.Serializable inObj, String fileName) throws Exception {
        FileOutputStream out;
        try {
            FileInputStream fsKeysIn;
            FileOutputStream fsKeysOut;
            Key key = null;
            byte[] raw;
            SecretKey skey = null;
            if ((keyFile == null) || (keyFile.length() == 0)) {
                fsKeysIn = null;
            } else {
                try {
                    fsKeysIn = new FileInputStream(keyFile);
                } catch (FileNotFoundException fnfE) {
                    fsKeysIn = null;
                }
            }
            KeyStore ks = KeyStore.getInstance("JCEKS");
            ks.load(fsKeysIn, keyPasswd.toCharArray());
            if (fsKeysIn != null) {
                fsKeysIn.close();
            }
            try {
                key = ks.getKey(fileName, filePasswd.toCharArray());
                if (key != null) {
                    if (key.getAlgorithm().compareTo(algorithm) != 0) {
                        key = null;
                    }
                }
            } catch (KeyStoreException ksE) {
                Log.out(ksE);
            } catch (NoSuchAlgorithmException nsaE) {
                Log.out(nsaE);
            } catch (UnrecoverableKeyException urkE) {
                Log.out(urkE);
            }
            out = new FileOutputStream(fileName);
            ObjectOutputStream s = new ObjectOutputStream(out);
            cipher = Cipher.getInstance(algorithm, "BC");
            if (key == null) {
                KeyGenerator kgen = KeyGenerator.getInstance(algorithm);
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
                try {
                    kgen.init(128, random);
                } catch (InvalidParameterException iPE) {
                    kgen.init(56, random);
                }
                skey = kgen.generateKey();
                raw = skey.getEncoded();
            } else {
                raw = key.getEncoded();
            }
            SecretKeySpec skeySpec = new SecretKeySpec(raw, algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            SealedObject so = new SealedObject(inObj, cipher);
            s.writeObject(so);
            s.flush();
            if (skey != null) {
                ks.setKeyEntry(fileName, skey, filePasswd.toCharArray(), null);
                if (keyFile.length() == 0) {
                    fsKeysOut = new FileOutputStream("SDM.keystore");
                } else {
                    fsKeysOut = new FileOutputStream(keyFile);
                }
                ks.store(fsKeysOut, keyPasswd.toCharArray());
                fsKeysOut.close();
            }
            out.close();
        } catch (Exception e) {
            Log.out(e);
            throw e;
        }
        return true;
    }

    public java.io.Serializable ReadFile(String fileName) throws Exception {
        FileInputStream in;
        FileInputStream fsKeysIn;
        FileOutputStream fsKeysOut;
        Key key = null;
        byte[] raw;
        SecretKey skey = null;
        java.io.Serializable retObj;
        try {
            if ((keyFile == null) || (keyFile.length() == 0)) {
                fsKeysIn = null;
            } else {
                try {
                    fsKeysIn = new FileInputStream(keyFile);
                } catch (FileNotFoundException fnfE) {
                    fsKeysIn = null;
                    throw new Exception("KeyStore file not found", fnfE);
                }
            }
            KeyStore ks = KeyStore.getInstance("JCEKS");
            try {
                ks.load(fsKeysIn, filePasswd.toCharArray());
            } catch (IOException ioE) {
                throw new Exception("KeyStore corrupt or bad keystore password", ioE);
            }
            if (fsKeysIn != null) {
                fsKeysIn.close();
            }
            try {
                key = ks.getKey(fileName, filePasswd.toCharArray());
                if (key != null) {
                    if (key.getAlgorithm().compareTo(algorithm) != 0) {
                        key = null;
                    }
                }
            } catch (KeyStoreException ksE) {
                Log.out(ksE);
                throw ksE;
            } catch (NoSuchAlgorithmException nsaE) {
                Log.out(nsaE);
                throw nsaE;
            } catch (UnrecoverableKeyException urkE) {
                Log.out(urkE);
                throw urkE;
            }
            in = new FileInputStream(fileName);
            cipher = Cipher.getInstance(algorithm);
            if (key == null) {
                KeyGenerator kgen = KeyGenerator.getInstance(algorithm);
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
                try {
                    kgen.init(128, random);
                } catch (InvalidParameterException iPE) {
                    kgen.init(56, random);
                }
                skey = kgen.generateKey();
                raw = skey.getEncoded();
            } else {
                raw = key.getEncoded();
            }
            SecretKeySpec skeySpec = new SecretKeySpec(raw, algorithm);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            ObjectInputStream s = new ObjectInputStream(in);
            SealedObject so = (SealedObject) s.readObject();
            retObj = (java.io.Serializable) so.getObject(cipher);
            in.close();
        } catch (Exception e) {
            throw e;
        }
        return retObj;
    }
}
