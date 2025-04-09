package com.jorenwu.asymmetric;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Random;

public class RSAengine {

    private BigInteger p, q, e, d, n, phi;

    private boolean forEncryption;

    private int nbits;

    public RSAengine(int nbits) {
        this.nbits = nbits;
    }

    /**
	 * generate all the keys
	 */
    public void keyGenerate() {
        Random rand = new Random(System.currentTimeMillis());
        e = new BigInteger(20, 15, rand);
        int pbitlength = (nbits + 1) / 2;
        int qbitlength = (nbits - pbitlength);
        PrimeTest pt = new MillerRabinPrimeTest();
        BaseCalculator bc = new BaseCalculator();
        for (; ; ) {
            p = bc.getBigRandomInt(pbitlength);
            if (p.mod(e).equals(BigInteger.ONE)) {
                continue;
            }
            if (!pt.primeTest(p, 15)) {
                continue;
            }
            if (e.gcd(p.subtract(BigInteger.ONE)).equals(BigInteger.ONE)) {
                break;
            }
        }
        for (; ; ) {
            q = bc.getBigRandomInt(qbitlength);
            if (q.equals(p)) {
                continue;
            }
            if (q.mod(e).equals(BigInteger.ONE)) {
                continue;
            }
            if (!pt.primeTest(q, 15)) {
                continue;
            }
            if (e.gcd(q.subtract(BigInteger.ONE)).equals(BigInteger.ONE)) {
                break;
            }
        }
        n = p.multiply(q);
        phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        d = e.modInverse(phi);
    }

    public void saveKey(String fileName) {
        try {
            BigInteger[] keyPair1 = new BigInteger[2];
            FileOutputStream f1 = new FileOutputStream(fileName + ".public");
            ObjectOutput s1 = new ObjectOutputStream(f1);
            keyPair1[0] = e;
            keyPair1[1] = n;
            s1.writeObject(keyPair1);
            BigInteger[] keyPair2 = new BigInteger[5];
            FileOutputStream f2 = new FileOutputStream(fileName + ".private");
            ObjectOutput s2 = new ObjectOutputStream(f2);
            keyPair2[0] = p;
            keyPair2[1] = q;
            keyPair2[2] = n;
            keyPair2[3] = d;
            keyPair2[4] = phi;
            s2.writeObject(keyPair2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
	 * get and set for the public keys n,b
	 */
    public void getFilePublicKey(String filename) {
        BigInteger[] privateKeyPair = new BigInteger[2];
        try {
            FileInputStream in = new FileInputStream(filename);
            ObjectInput s = new ObjectInputStream(in);
            privateKeyPair = (BigInteger[]) s.readObject();
            e = privateKeyPair[0];
            n = privateKeyPair[1];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
	 * get and set for the private keys p,q,n,b
	 */
    public void getFilePrivateKey(String filename) {
        BigInteger[] publicKeyPair = new BigInteger[5];
        try {
            FileInputStream in = new FileInputStream(filename);
            ObjectInput s = new ObjectInputStream(in);
            publicKeyPair = (BigInteger[]) s.readObject();
            p = publicKeyPair[0];
            q = publicKeyPair[1];
            n = publicKeyPair[2];
            d = publicKeyPair[3];
            phi = publicKeyPair[4];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    BigInteger input;

    public String numProcess() {
        if (forEncryption) {
            if (d == null && n == null) {
                System.out.println("the private key hasn't been initialized!");
                return null;
            }
            if (input == null) {
                System.out.println("the plain text hasn't been inputed!");
                return null;
            }
            input = input.modPow(e, n);
            return input.toString();
        } else {
            if (e == null && n == null) {
                System.out.println("the public key hasn't initialized!");
                return null;
            }
            if (input == null) {
                System.out.println("the encrypt text hasn't been inputed!");
                return null;
            }
            input = input.modPow(d, n);
            return input.toString();
        }
    }

    RSAEncoder encoder = new RSAEncoder();

    /**
	 * encode the plain text
	 */
    public void blockEncrypto(Reader reader, OutputStream outputStream) {
        forEncryption = true;
        BigInteger bi;
        if (e == null && n == null && reader == null && outputStream == null) {
            System.out.println("there are some values you haven't initialized!");
            System.exit(0);
        }
        try {
            int ch = reader.read();
            while (ch >= 0) {
                bi = encoder.inputBlockEncrypto(ch);
                bi = bi.modPow(e, n);
                encoder.outputBlockEncrypto(outputStream, bi);
                ch = reader.read();
            }
            outputStream.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * decode the secrete text
	 */
    public void blockDecrypto(Writer writer, InputStream inputStream) {
        forEncryption = false;
        BigInteger bi;
        if (d == null && n == null && writer == null && inputStream == null) {
            System.out.println("there are some values you haven't initialized!");
            System.exit(0);
        }
        try {
            byte[] bt = new byte[129];
            while (inputStream.read(bt) >= 0) {
                bi = encoder.inputBlockDecrypto(bt);
                bi = bi.modPow(d, n);
                encoder.outputBlockDecrypto(writer, bi);
            }
            inputStream.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BigInteger getP() {
        return p;
    }

    public void setP(BigInteger p) {
        this.p = p;
    }

    public BigInteger getQ() {
        return q;
    }

    public void setQ(BigInteger q) {
        this.q = q;
    }

    public BigInteger getN() {
        return n;
    }

    public void setN(BigInteger n) {
        this.n = n;
    }

    public BigInteger getPublicKey() {
        return e;
    }

    public void setPublicKey(BigInteger e) {
        this.e = e;
    }

    public BigInteger getPrivateKey() {
        return d;
    }

    public void setPrivateKey(BigInteger d) {
        this.d = d;
    }

    public boolean getForEncryption() {
        return forEncryption;
    }

    public void setForEncryption(boolean forEncryption) {
        this.forEncryption = forEncryption;
    }

    public void setInput(BigInteger input) {
        this.input = input;
    }
}
