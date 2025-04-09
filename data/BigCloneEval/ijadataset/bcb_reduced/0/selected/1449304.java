package com.jorenwu.asymmetric;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;

public class Rabinengine {

    private BigInteger p, q, n, b;

    private int nbits;

    public Rabinengine(int nbits) {
        this.nbits = nbits;
    }

    /**
	 * generate all the keys
	 */
    public void keyGenerate() {
        int pbitlength = (nbits + 1) / 2;
        int qbitlength = (nbits - pbitlength);
        PrimeTest pt = new MillerRabinPrimeTest();
        BaseCalculator bc = new BaseCalculator();
        for (; ; ) {
            p = bc.getBigRandomInt(pbitlength);
            if (!pt.primeTest(p, 15)) {
                continue;
            }
            break;
        }
        for (; ; ) {
            q = bc.getBigRandomInt(qbitlength);
            if (q.equals(p)) {
                continue;
            }
            if (!pt.primeTest(q, 15)) {
                continue;
            }
            break;
        }
        n = p.multiply(q);
        for (; ; ) {
            b = bc.getBigRandomInt(pbitlength);
            if (!b.gcd(n).equals(BigInteger.ONE)) {
                continue;
            }
            break;
        }
    }

    public void saveKey(String fileName) {
        try {
            BigInteger[] keyPair1 = new BigInteger[2];
            FileOutputStream f1 = new FileOutputStream(fileName + ".public");
            ObjectOutput s1 = new ObjectOutputStream(f1);
            keyPair1[0] = n;
            keyPair1[1] = b;
            s1.writeObject(keyPair1);
            BigInteger[] keyPair2 = new BigInteger[4];
            FileOutputStream f2 = new FileOutputStream(fileName + ".private");
            ObjectOutput s2 = new ObjectOutputStream(f2);
            keyPair2[0] = p;
            keyPair2[1] = q;
            keyPair2[2] = n;
            keyPair2[3] = b;
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
            n = privateKeyPair[0];
            b = privateKeyPair[1];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
	 * get and set for the private keys p,q,n,b
	 */
    public void getFilePrivateKey(String filename) {
        BigInteger[] publicKeyPair = new BigInteger[4];
        try {
            FileInputStream in = new FileInputStream(filename);
            ObjectInput s = new ObjectInputStream(in);
            publicKeyPair = (BigInteger[]) s.readObject();
            p = publicKeyPair[0];
            q = publicKeyPair[1];
            n = publicKeyPair[2];
            b = publicKeyPair[3];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    JorenEncoder encoder = new JorenEncoder();

    public void blockEncrypto(Reader reader, ObjectOutputStream outputStream) {
        if (n == null && b == null && reader == null && outputStream == null) {
            System.out.println("there are some values you haven't initialized!");
            System.exit(0);
        }
        try {
            BigInteger c, m;
            int ch = reader.read();
            while (ch >= 0) {
                m = encoder.inputBlockEncrypto(ch);
                c = m.multiply((m.add(b))).mod(n);
                encoder.outputBlockEncrypto(outputStream, c);
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
    public void blockDecrypto(Writer writer, ObjectInputStream inputStream) {
        if (q == null && p == null && writer == null && inputStream == null) {
            System.out.println("there are some values you haven't initialized!");
            System.exit(0);
        }
        try {
            BigInteger p1 = p.add(BigInteger.ONE).divide(new BigInteger("4"));
            BigInteger q1 = q.add(BigInteger.ONE).divide(new BigInteger("4"));
            BigInteger bi = null, m[] = new BigInteger[4], deta = null;
            int i;
            Media c = (Media) inputStream.readObject();
            while (true) {
                bi = encoder.inputBlockDecrypto(c);
                deta = b.pow(2).add(bi.multiply(new BigInteger("4")));
                m[0] = deta.modPow(p1, p);
                m[1] = p.subtract(m[0]);
                m[2] = deta.modPow(q1, q);
                m[3] = q.subtract(m[0]);
                for (i = 0; i < 4; i++) {
                }
                encoder.outputBlockDecrypto(writer, m[i]);
                try {
                    c = (Media) inputStream.readObject();
                } catch (Exception ex) {
                    break;
                }
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
        return b;
    }

    public void setPublicKey(BigInteger b) {
        this.b = b;
    }
}
