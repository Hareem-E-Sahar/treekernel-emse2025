package gov.lanl.MakeTestKeys;

import java.security.*;
import java.security.cert.CertificateException;
import java.io.*;
import java.util.*;
import java.math.BigInteger;
import iaik.asn1.*;
import iaik.asn1.structures.*;
import iaik.pkcs.pkcs8.*;
import iaik.security.provider.IAIK;
import iaik.security.rsa.*;
import iaik.x509.*;
import iaik.x509.extensions.*;
import javax.crypto.Cipher;
import java.text.*;

/**
 *This class makes a private/public key pair, encrypts the private key
 *with the password, writes out the private key and a certificate with the
 *pub keys to a floppy disk
 *as well as a encrypted (with the private key) string.  The keys are
 *written with the user name for the file name and .prv or .pub extensions
 *@author Jim George
 *@version 4/13/98
 */
public class MakeRSAKeys {

    String loginName = new String("name");

    String password = new String("password");

    String realName = new String("realname");

    String policies = new String("*READ*");

    String dirname = new String("a:/");

    String algorithm = new String("RSA");

    int numbits = 512;

    String dirpathname;

    public KeyPair generateKeyPair(String algorithm, int numbits) {
        KeyPairGenerator generator = null;
        System.out.println("  generating a " + algorithm + " KeyPair..." + " for " + numbits + " bits");
        try {
            generator = KeyPairGenerator.getInstance(algorithm, "IAIK");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("NoSuchAlgorithmException: " + ex.toString());
        } catch (NoSuchProviderException ex) {
            System.out.println("NoSuchProviderException: " + ex.toString());
        }
        generator.initialize(numbits);
        KeyPair keyPair = generator.generateKeyPair();
        System.out.println("    generateKeyPair done");
        return keyPair;
    }

    public X509Certificate createCertificate(Name subject, PublicKey pk, Name issuer, PrivateKey sk, AlgorithmID algorithm, boolean extensions) {
        byte[] id = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf };
        X509Certificate cert = new X509Certificate();
        cert.setSerialNumber(BigInteger.valueOf(0x1234L));
        cert.setSubjectDN(subject);
        try {
            cert.setPublicKey(pk);
        } catch (InvalidKeyException ex) {
        }
        cert.setIssuerDN(issuer);
        GregorianCalendar date = new GregorianCalendar();
        cert.setValidNotBefore(date.getTime());
        System.out.println("Start date = " + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR));
        date.add(Calendar.MONTH, 24);
        cert.setValidNotAfter(date.getTime());
        System.out.println("  End date = " + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR));
        if (extensions) {
            try {
                SubjectKeyIdentifier ski = new SubjectKeyIdentifier(id);
                cert.addExtension(ski);
                BasicConstraints bc = new BasicConstraints(true, 1);
                bc.setCritical(true);
                cert.addExtension(bc);
                KeyUsage ku = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign);
                cert.addExtension(ku);
            } catch (Exception xee) {
                System.out.println("X509 Extension Exception " + xee);
            }
        }
        try {
            cert.sign(algorithm, sk);
        } catch (CertificateException ex) {
            System.out.println("CertificateException: " + ex);
            return null;
        } catch (InvalidKeyException ex) {
            System.out.println("KeyException: " + ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("NoSuchAlgorithmException: " + ex);
            return null;
        }
        return cert;
    }

    public void writeX509Certificate(RSAPrivateKey pk, RSAPublicKey pubk) {
        System.out.println("  Writing RSA-X509 Certificate");
        Name theUser = new Name();
        theUser.addRDN(ObjectID.country, "USA");
        theUser.addRDN(ObjectID.organization, "LANL");
        theUser.addRDN(ObjectID.organizationalUnit, "TeleMed");
        theUser.addRDN(ObjectID.commonName, realName);
        System.out.println("create user signed certificate");
        X509Certificate userCert = createCertificate(theUser, pubk, theUser, pk, AlgorithmID.md5WithRSAEncryption, false);
        File whichfile = new File(dirpathname, "public");
        System.out.println("  Writing cert to " + whichfile);
        try {
            OutputStream thefile = new FileOutputStream(whichfile);
            userCert.writeTo(thefile);
            thefile.close();
        } catch (Exception e) {
            System.out.println("in writeX509.. " + e);
        }
    }

    public void encryptAndWrite(File thefile, Cipher theCipher, byte[] sample, int blksize) {
        OutputStream sampleOutStream;
        int numtodo;
        byte[] encrypted;
        try {
            System.out.println("    writing to " + thefile);
            sampleOutStream = new FileOutputStream(thefile);
            encrypted = new byte[0];
            for (int i = 0; i < sample.length; i = i + blksize) {
                numtodo = sample.length - i;
                if (numtodo > blksize) numtodo = blksize;
                encrypted = theCipher.doFinal(sample, i, numtodo);
                sampleOutStream.write(encrypted);
            }
            sampleOutStream.close();
        } catch (Exception e) {
            System.out.println("in encryptAndWrite: " + e);
            e.printStackTrace();
        }
    }

    public void writeEncryptedRSASamples(RSAPrivateKey pk, RSAPublicKey pubk) {
        Cipher rsa;
        int blksize;
        byte[] plainData;
        try {
            blksize = pubk.getModulus().toString(16).length() / 2 - 2;
            System.out.println("  Writing sample with RSA public key, blocksize=" + blksize);
            plainData = new String("This is a test encrypted with the public key\n " + "and decrypted with the private key!\n by jeg 3/25/98\n" + "This is the final line just to get a long string out").getBytes();
            rsa = Cipher.getInstance("RSA/ECB/NoPadding");
            rsa.init(Cipher.ENCRYPT_MODE, pubk);
            encryptAndWrite(new File(dirpathname, "sample.pub"), rsa, plainData, blksize);
            blksize = pk.getModulus().toString(16).length() / 2 - 2;
            System.out.println("  Writing sample with RSA private key, blocksize=" + blksize);
            plainData = new String("This is a test encrypted with the private key\n " + "and decrypted with the public key!\n by jeg 3/25/98\n" + "This is the final line just to get a long string out").getBytes();
            rsa = Cipher.getInstance("RSA/ECB/NoPadding");
            rsa.init(Cipher.ENCRYPT_MODE, pk);
            encryptAndWrite(new File(dirpathname, "sample.pri"), rsa, plainData, blksize);
        } catch (Exception e) {
            System.out.println("in writeEncryptedRSASamples: " + e);
        }
    }

    public void generateRSAKeyDisk() {
        KeyPair keyPair;
        System.out.println("Generating a RSA key disk for " + loginName + " with password " + password);
        keyPair = generateKeyPair(algorithm, numbits);
        RSAPrivateKey pk = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey pubk = (RSAPublicKey) keyPair.getPublic();
        writeEncryptedRSASamples(pk, pubk);
        writeX509Certificate(pk, pubk);
        try {
            EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(pk);
            encryptedPrivateKeyInfo.encrypt(password, AlgorithmID.pbeWithMD5AndDES_CBC, null);
            System.out.println("  Writing private key");
            OutputStream theKeyOutStream = new FileOutputStream(new File(dirpathname, "private"));
            encryptedPrivateKeyInfo.writeTo(theKeyOutStream);
            theKeyOutStream.close();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("NoSuchAlgorithmException: " + ex.toString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**Creates a directory named loginName and write the data.txt file
    */
    public void createDirAndWriteData() {
        byte[] newline = (new String("\n")).getBytes();
        try {
            File afile = new File(dirname, loginName);
            afile.mkdirs();
            dirpathname = afile.getPath();
            afile = new File(dirpathname, "data.txt");
            OutputStream thefile = new FileOutputStream(afile);
            thefile.write(loginName.getBytes());
            thefile.write(newline);
            thefile.write(password.getBytes());
            thefile.write(newline);
            thefile.write(realName.getBytes());
            thefile.write(newline);
            thefile.write(policies.getBytes());
            thefile.write(newline);
            thefile.close();
        } catch (Exception e) {
            System.out.println("in createDirAndWriteData " + e);
        }
    }

    /**
		* Performs certificate creation and verification tests.
		*@exception IOException if an I/O Error occurs
		*/
    public static void main(String arg[]) throws IOException {
        String tmpbuf;
        MakeRSAKeys keydisk = new MakeRSAKeys();
        BufferedReader inbuf = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("\n-----Making a RSA Key Disk for a Telemed User-----\n");
        System.out.print("\nEnter login name ? ");
        keydisk.loginName = inbuf.readLine();
        System.out.print("\nEnter password ? ");
        keydisk.password = inbuf.readLine();
        System.out.print("\nEnter real name ? ");
        keydisk.realName = inbuf.readLine();
        System.out.print("\nEnter policies ? ");
        keydisk.policies = inbuf.readLine();
        System.out.print("\nEnter directory (" + keydisk.dirname + ") ? ");
        tmpbuf = inbuf.readLine();
        if (tmpbuf.length() > 0) keydisk.dirname = tmpbuf;
        keydisk.createDirAndWriteData();
        System.out.println("\nadd Provider IAIK...\n");
        IAIK.addAsProvider(true);
        keydisk.generateRSAKeyDisk();
        System.out.println("Finished, <cr> to terminate");
        System.in.read();
    }
}
