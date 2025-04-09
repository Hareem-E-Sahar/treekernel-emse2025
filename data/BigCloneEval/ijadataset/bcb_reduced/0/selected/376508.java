package de.bea.environment.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Programm zum Verschl�sseln von Property-Dateien. Diese Klasse sollte nicht
 * Bestandteil einer Release-Version sein. Sie wird vom Ant-Skript verwendet um
 * alle Properties-Dateien in einem Verzeichnis zu Verschl�sseln. Der
 * Key wird in einer Properties Datei abgelegt. Die Applikation liest
 * diesen Key aus um die Property-Dateien wieder zu entschl�sseln.
 */
public class EncodeProperties {

    private final SecretKey key;

    private final Cipher cipher;

    /**
     * Constructor for EncodeProperties.
     */
    private EncodeProperties() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("DES");
            key = generator.generateKey();
            cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String keyRepresentation(byte[] key) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < key.length; i++) {
            int val = key[i];
            if (val < 0) {
                val += 256;
            }
            int hi = val >> 4;
            int lo = val & 15;
            char c_hi = (char) (hi < 10 ? '0' + hi : 'A' + hi - 10);
            char c_lo = (char) (lo < 10 ? '0' + lo : 'A' + lo - 10);
            buffer.append(c_hi);
            buffer.append(c_lo);
        }
        return buffer.toString();
    }

    private void run(File sourceDir, File destDir, File propertyFile) {
        String[] names = sourceDir.list();
        for (int i = 0; i < names.length; i++) {
            if (names[i].endsWith(".properties")) {
                encode(sourceDir, destDir, names[i]);
            }
            File dir = new File(sourceDir, names[i]);
            if (dir.isDirectory()) {
                run(dir, new File(destDir, names[i]), null);
            }
        }
        if (propertyFile != null) {
            try {
                FileInputStream in = new FileInputStream(propertyFile);
                Properties properties = new Properties();
                properties.load(in);
                in.close();
                DESKeySpec spec = (DESKeySpec) SecretKeyFactory.getInstance("DES").getKeySpec(key, DESKeySpec.class);
                properties.put("envKey", keyRepresentation(spec.getKey()));
                FileOutputStream out = new FileOutputStream(propertyFile);
                properties.store(out, "Environment Info");
                out.close();
            } catch (Exception e) {
                System.err.println("Exception: " + e);
            }
        }
    }

    private void encode(File sourceDir, File destDir, String filename) {
        System.out.println("Encoding file: " + filename);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            FileInputStream in = new FileInputStream(new File(sourceDir, filename));
            FileOutputStream out = new FileOutputStream(new File(destDir, filename));
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) >= 0) {
                out.write(cipher.update(buffer, 0, count));
            }
            out.write(cipher.doFinal());
            in.close();
            out.close();
        } catch (Exception e) {
            System.err.println("Exception occured: " + e);
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 3) {
            System.err.println("Usage: sourceDir destDir envInfo.properties");
            throw new IllegalArgumentException();
        }
        File sourceDir = new File(args[0]);
        File destDir = new File(args[1]);
        File propertyFile = new File(args[2]);
        EncodeProperties encodeProperties = new EncodeProperties();
        encodeProperties.run(sourceDir, destDir, propertyFile);
    }
}
