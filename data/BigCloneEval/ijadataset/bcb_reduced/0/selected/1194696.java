package cornell.herbivore.system;

import cornell.herbivore.util.Log;
import cornell.herbivore.util.*;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.security.*;
import xjava.security.*;
import cryptix.provider.rsa.*;

public class HerbivoreEndpoint {

    private HerbivoreRSAKeyPair keypair;

    protected HerbivoreEndpoint(String epname, HerbivoreRSAKeyPair akeypair) {
        keypair = akeypair;
    }

    protected HerbivoreEndpoint(byte[] pubkey) {
        keypair = new HerbivoreRSAKeyPair(pubkey, null);
    }

    protected HerbivoreEndpoint(String epname, boolean pubonly) {
        try {
            byte[] publicKeyBytes = null;
            byte[] privateKeyBytes = null;
            PublicKey publicKey = new RawRSAPublicKey(new FileInputStream(epname + ".pubkey"));
            publicKeyBytes = publicKey.getEncoded();
            if (!pubonly) {
                PrivateKey privateKey = new RawRSAPrivateKey(new FileInputStream(epname + ".privkey"));
                privateKeyBytes = privateKey.getEncoded();
            }
            Log.info("Read public/private endpoint keys from disk...");
            keypair = new HerbivoreRSAKeyPair(publicKeyBytes, privateKeyBytes);
            return;
        } catch (Exception e) {
            System.out.println("Can't read public/private endpoint keys from file.");
        }
        keypair = HerbivoreRSA.generateKeyPair();
        try {
            FileOutputStream fos = null;
            System.out.println("Saving endpoint keys to disk...");
            fos = new FileOutputStream(epname + ".pubkey");
            fos.write(keypair.getPublicKey());
            fos.close();
            fos = new FileOutputStream(epname + ".privkey");
            fos.write(keypair.getPrivateKey());
            fos.close();
        } catch (Exception e) {
            System.out.println("Can't save endpoint keys to file.");
        }
    }

    public byte[] getPublicKey() {
        return keypair.getPublicKey();
    }

    public byte[] getPrivateKey() {
        return keypair.getPrivateKey();
    }
}
