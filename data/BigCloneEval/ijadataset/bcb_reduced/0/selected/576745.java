package hr.fer.pus.dll_will.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class GenerateKeysCR {

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        KeyPairGenerator gen;
        try {
            gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(1024);
            KeyPair pair = gen.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) pair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) pair.getPublic();
            String privExp = privateKey.getPrivateExponent().toString();
            String publExp = publicKey.getPublicExponent().toString();
            String modulus = publicKey.getModulus().toString();
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("web/private/cr_private_key.key")));
            String lineSep = System.getProperty("line.separator");
            bw.write(modulus);
            bw.write(lineSep);
            bw.write(privExp);
            bw.close();
            bw = new BufferedWriter(new FileWriter(new File("web/private/cr_public_key.key")));
            bw.write(modulus);
            bw.write(lineSep);
            bw.write(publExp);
            bw.close();
        } catch (NoSuchAlgorithmException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
