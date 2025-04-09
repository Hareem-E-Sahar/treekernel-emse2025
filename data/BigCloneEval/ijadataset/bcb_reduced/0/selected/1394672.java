package org.tscribble.bitleech.core.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class RSA implements Cryptography {

    private static final String RSA_ASSIMETRIC_ALGORITHM = "RSA";

    private static final String UTF_8 = "UTF-8";

    private static PrintStream err = System.out;

    private KeyPair keyPair;

    private Cipher dcipher;

    private Cipher ecipher;

    private static int EXIT = 0;

    private void initialize(KeyPair keyPair) {
        if (keyPair == null) {
            throw new NullPointerException("Par de chaves inexistente");
        } else if (keyPair.getPrivate() == null) {
            throw new NullPointerException("Chave privada inexistente");
        } else if (keyPair.getPublic() == null) {
            throw new NullPointerException("Chave publica inexistente");
        }
        try {
            this.keyPair = keyPair;
            dcipher = Cipher.getInstance(RSA.RSA_ASSIMETRIC_ALGORITHM);
            ecipher = Cipher.getInstance(RSA.RSA_ASSIMETRIC_ALGORITHM);
            ecipher.init(Cipher.ENCRYPT_MODE, this.keyPair.getPublic());
            dcipher.init(Cipher.DECRYPT_MODE, this.keyPair.getPrivate());
        } catch (javax.crypto.NoSuchPaddingException e) {
            err.println("Padding inexistente.");
        } catch (java.security.NoSuchAlgorithmException e) {
            err.println("Algoritmo criptografico inexistente.");
        } catch (java.security.InvalidKeyException e) {
            err.println("Par de chaves inv?lidas.");
        }
    }

    public RSA(KeyPair keyPair) throws NullPointerException {
        this.initialize(keyPair);
    }

    public RSA(String filePath) throws NullPointerException {
        KeyPair key = getKeyPair(filePath);
        if (key == null) {
            err.println("Par de cahves inv?lidas");
            System.exit(EXIT);
        }
        this.initialize(key);
    }

    public String encrypt(String data) {
        try {
            byte[] utf8 = data.getBytes(RSA.UTF_8);
            byte[] enc = ecipher.doFinal(utf8);
            return new sun.misc.BASE64Encoder().encode(enc);
        } catch (javax.crypto.BadPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    public String decrypt(String data) {
        try {
            byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(data);
            byte[] utf8 = dcipher.doFinal(dec);
            return new String(utf8, "UTF8");
        } catch (javax.crypto.BadPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (UnsupportedEncodingException e) {
        } catch (java.io.IOException e) {
        }
        return null;
    }

    private static KeyPair getKeyPair(String filePath) {
        File filePrivateKey = new File(filePath + "_PRIVATE");
        File filePublicKey = new File(filePath + "_PUBLIC");
        KeyPair keys = null;
        boolean existPrivateKeyFile = filePrivateKey.exists();
        boolean existPublicKeyFile = filePublicKey.exists();
        if (!existPrivateKeyFile && !existPublicKeyFile) {
            PrintWriter printFilePrivate = null;
            PrintWriter printFilePublic = null;
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                keys = keyPairGenerator.generateKeyPair();
                printFilePrivate = new PrintWriter(new FileWriter(filePrivateKey));
                printFilePublic = new PrintWriter(new FileWriter(filePublicKey));
                printFilePrivate.println(new sun.misc.BASE64Encoder().encode(keys.getPrivate().getEncoded()));
                printFilePublic.println(new sun.misc.BASE64Encoder().encode(keys.getPublic().getEncoded()));
            } catch (NoSuchAlgorithmException e) {
                err.println("Algoritmo criptogr?fico inv?lido.");
            } catch (IOException e) {
                err.println("Erro de I/O (Gravar em arquivo).");
            } finally {
                if (printFilePrivate != null) {
                    printFilePrivate.close();
                }
                if (printFilePublic != null) {
                    printFilePublic.close();
                }
            }
        } else {
            if (filePrivateKey.length() <= 1 || filePublicKey.length() <= 1) {
                filePrivateKey.delete();
                filePublicKey.delete();
                return getKeyPair(filePath);
            }
            String skeyPrivate = getKeyInFile(filePath + "_PRIVATE");
            String skeyPublic = getKeyInFile(filePath + "_PUBLIC");
            if (skeyPublic == null || skeyPrivate == null) {
                throw new NullPointerException("O arquivo n?o possui nenhuma \"key\"");
            }
            try {
                byte[] decodePrivateKey = new sun.misc.BASE64Decoder().decodeBuffer(skeyPrivate);
                byte[] decodePublicKey = new sun.misc.BASE64Decoder().decodeBuffer(skeyPublic);
                PKCS8EncodedKeySpec specPrivateKey = new PKCS8EncodedKeySpec(decodePrivateKey);
                X509EncodedKeySpec specPublicKey = new X509EncodedKeySpec(decodePublicKey);
                KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
                PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(specPrivateKey);
                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(specPublicKey);
                keys = new KeyPair(publicKey, privateKey);
            } catch (Exception e) {
                err.println(e.getMessage());
            }
        }
        return keys;
    }

    private static String getKeyInFile(String filePath) {
        BufferedReader fileReader = null;
        StringBuffer key = new StringBuffer();
        try {
            File file = new File(filePath);
            fileReader = new BufferedReader(new FileReader(file));
            String line = fileReader.readLine();
            while (line != null) {
                key.append(line);
                line = fileReader.readLine();
            }
        } catch (FileNotFoundException e) {
            err.println("Arquivo n?o encontrado.");
        } catch (IOException e) {
            err.println("Erro de I/O (Leitura do arquivo).");
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    err.println("Erro de I/O (Fechar arquivo).");
                }
            }
        }
        return key.toString();
    }
}
