package cx.fbn.nevernote.utilities;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypter {

    private Cipher cipher;

    private String password;

    private String userid;

    private final SecretKeySpec skeySpec;

    private final AlgorithmParameterSpec paramSpec;

    public AESEncrypter() {
        String key = "x331aq5wDQ8xO81v";
        skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        password = new String("");
        userid = new String("");
        byte[] iv = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
        paramSpec = new IvParameterSpec(iv);
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPassword(String s) {
        password = s;
    }

    public String getPassword() {
        return password;
    }

    public void setUserid(String s) {
        userid = s;
    }

    public String getUserid() {
        return userid;
    }

    public void encrypt(OutputStream out) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, paramSpec);
            out = new CipherOutputStream(out, cipher);
            StringBuffer u = new StringBuffer(1024);
            u.append("Userid:" + userid + " " + password);
            for (int i = u.length(); i < 128; i++) u.append('\0');
            out.write(u.toString().getBytes());
            out.close();
        } catch (java.io.IOException e) {
            System.out.println("Encrypt i/o exception");
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        } catch (InvalidAlgorithmParameterException e1) {
            e1.printStackTrace();
        }
    }

    public void decrypt(InputStream in) {
        byte[] buf = new byte[1024];
        try {
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, paramSpec);
            in = new CipherInputStream(in, cipher);
            if (in.read(buf) >= 0) {
                String line = new String(buf);
                int offset = line.indexOf(" ");
                if (offset > 0) {
                    userid = line.substring(line.indexOf(":") + 1, offset);
                    password = line.substring(offset + 1);
                    password = password.trim();
                }
            }
            in.close();
        } catch (java.io.IOException e) {
            return;
        } catch (InvalidKeyException e1) {
            return;
        } catch (InvalidAlgorithmParameterException e1) {
            return;
        }
    }
}
