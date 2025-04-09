package com.frame.util.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.crypto.Cipher;

/**
 * 功能：
 * 创建人： pioneer
 * 创建时间：2008-6-18
 * 修订人：
 * 修订时间：
 * 版本：v 1.0
 */
public class CryptFile {

    PublicKey pubKeyObj = null;

    PrivateKey priKeyObj = null;

    public void encryptFile(String srcFileName, String destFileName) throws Exception {
        OutputStream outputWriter = null;
        InputStream inputReader = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            byte[] buf = new byte[100];
            int bufl;
            cipher.init(Cipher.ENCRYPT_MODE, this.pubKeyObj);
            outputWriter = new FileOutputStream(destFileName);
            inputReader = new FileInputStream(srcFileName);
            while ((bufl = inputReader.read(buf)) != -1) {
                byte[] encText = null;
                byte[] newArr = null;
                if (buf.length == bufl) {
                    newArr = buf;
                } else {
                    newArr = new byte[bufl];
                    for (int i = 0; i < bufl; i++) {
                        newArr[i] = (byte) buf[i];
                    }
                }
                encText = cipher.doFinal(newArr);
                outputWriter.write(encText);
            }
            outputWriter.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (outputWriter != null) {
                    outputWriter.close();
                }
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public void decryptFile(String srcFileName, String destFileName) throws Exception {
        OutputStream outputWriter = null;
        InputStream inputReader = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
            byte[] buf = new byte[128];
            int bufl;
            cipher.init(Cipher.DECRYPT_MODE, this.priKeyObj);
            outputWriter = new FileOutputStream(destFileName);
            inputReader = new FileInputStream(srcFileName);
            while ((bufl = inputReader.read(buf)) != -1) {
                byte[] encText = null;
                byte[] newArr = null;
                if (buf.length == bufl) {
                    newArr = buf;
                } else {
                    newArr = new byte[bufl];
                    for (int i = 0; i < bufl; i++) {
                        newArr[i] = (byte) buf[i];
                    }
                }
                encText = cipher.doFinal(newArr);
                outputWriter.write(encText);
            }
            outputWriter.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (outputWriter != null) {
                    outputWriter.close();
                }
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static void createKeyPars(String dir) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(1024);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            String prvKeyFile = dir + "privateKey.scrpt";
            String pubKeyFile = dir + "publicKey.scrpt";
            FileOutputStream prvfos = new FileOutputStream(prvKeyFile);
            ObjectOutputStream prvoos = new ObjectOutputStream(prvfos);
            prvoos.writeObject(privateKey);
            prvoos.close();
            prvfos.close();
            FileOutputStream pubfos = new FileOutputStream(pubKeyFile);
            ObjectOutputStream puboos = new ObjectOutputStream(pubfos);
            puboos.writeObject(publicKey);
            puboos.close();
            pubfos.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPrvKey(String path) {
        if (null != path) {
            try {
                FileInputStream inf = new FileInputStream(path);
                ObjectInputStream inobj = new ObjectInputStream(inf);
                RSAPrivateKey prvKey = (RSAPrivateKey) inobj.readObject();
                this.setPriKeyObj(prvKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static RSAPrivateKey getPrvKey(String path) {
        if (null != path) {
            try {
                FileInputStream inf = new FileInputStream(path);
                ObjectInputStream inobj = new ObjectInputStream(inf);
                RSAPrivateKey prvKey = (RSAPrivateKey) inobj.readObject();
                return prvKey;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static RSAPublicKey getPubKey(String path) {
        if (null != path) {
            try {
                FileInputStream inf = new FileInputStream(path);
                ObjectInputStream inobj = new ObjectInputStream(inf);
                RSAPublicKey pubKey = (RSAPublicKey) inobj.readObject();
                return pubKey;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            CryptFile cryptf = new CryptFile();
            cryptf.encodeFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void encodeFile() throws Exception {
        RSAPrivateKey privateKey = getPrvKey("D:/crpt/softsign/privateKey.scrpt");
        RSAPublicKey publicKey = getPubKey("D:/crpt/softsign/publicKey.scrpt");
        CryptFile crpt = new CryptFile();
        crpt.setPubKeyObj(publicKey);
        crpt.encryptFile("j:/application.xml", "j://appconfig.xml");
    }

    public PrivateKey getPriKeyObj() {
        return priKeyObj;
    }

    public void setPriKeyObj(PrivateKey priKeyObj) {
        this.priKeyObj = priKeyObj;
    }

    public PublicKey getPubKeyObj() {
        return pubKeyObj;
    }

    public void setPubKeyObj(PublicKey pubKeyObj) {
        this.pubKeyObj = pubKeyObj;
    }
}
