package uk.ac.warwick.dcs.cokefolk.util;

import java.io.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class CryptoClassLoader extends ClassLoader {

    public static final String EXTENSION = ".crypt";

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = null;
        try {
            System.out.println("Crypto class loader loading " + name);
            classBytes = loadClassBytes(name);
        } catch (IOException e) {
            throw new ClassNotFoundException(name);
        }
        Class cl = defineClass(name, classBytes, 0, classBytes.length);
        if (cl == null) {
            throw new ClassNotFoundException(name);
        }
        System.out.println("Crypto class loader loaded " + name);
        return cl;
    }

    private byte[] loadClassBytes(String name) throws IOException {
        String filename = name.replace(".", "/") + EXTENSION;
        FileInputStream in = null;
        byte[] bytecode = null;
        try {
            in = new FileInputStream(filename);
            bytecode = cokefolk(in, Cipher.DECRYPT_MODE);
        } finally {
            if (in != null) in.close();
        }
        return bytecode;
    }

    private static byte[] cokefolk(InputStream in, int mode) throws IOException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(cokefolk2("������������ܼ�˼��������"));
            byte[] keyData = new byte[] { -14, -40, 28, 19, 7, -127, 124, -15, -102, 8, 99, -36, -21, 56, -15, -25, -79, -39, -70, -48, -91, -63, 22, 27 };
            Key key = new SecretKeySpec(keyData, cokefolk2("��������"));
            cipher.init(mode, key);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int blockSize = cipher.getBlockSize();
            byte[] inBytes = new byte[cipher.getOutputSize(blockSize)];
            int inLength = 0;
            boolean more = true;
            while (more) {
                inLength = in.read(inBytes);
                if (inLength == blockSize) {
                    out.write(cipher.update(inBytes, 0, blockSize));
                } else {
                    more = false;
                }
            }
            if (inLength > 0) {
                out.write(cipher.doFinal(inBytes, 0, inLength));
            } else {
                out.write(cipher.doFinal());
            }
            return out.toByteArray();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IOException(e.getLocalizedMessage());
        }
    }

    private static String cokefolk2(String str) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] sourceBytes;
        try {
            sourceBytes = str.getBytes("ISO-8859-1");
            for (int index = 0; index < sourceBytes.length; index++) {
                byte b = (byte) (255 - sourceBytes[index] + index);
                buffer.write(b);
            }
            return new String(buffer.toByteArray(), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("All Java platforms should support the Latin-1 character set.");
        }
    }

    public static void main(String[] argsv) {
        String directory = ".";
        if (argsv.length > 0) directory = argsv[0];
        encryptClasses(new File(directory));
    }

    public static void encryptClasses(File directory) {
        for (File file : directory.listFiles()) {
            String filename = file.getPath();
            if (filename.endsWith(".class") && !filename.endsWith(EXTENSION)) {
                FileInputStream in = null;
                FileOutputStream out = null;
                byte[] bytecode = null;
                try {
                    in = new FileInputStream(file);
                    out = new FileOutputStream(filename.replace(".class", EXTENSION));
                    bytecode = cokefolk(in, Cipher.ENCRYPT_MODE);
                    System.out.println("Outputting encrypted " + filename);
                    out.write(bytecode);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                } finally {
                    if (in != null) try {
                        in.close();
                    } catch (IOException e) {
                    }
                    if (out != null) try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
            }
            if (file.isDirectory()) {
                encryptClasses(file);
            }
        }
    }
}
