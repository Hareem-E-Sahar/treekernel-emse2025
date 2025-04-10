package demo.pkcs.pkcs11;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import iaik.asn1.structures.AlgorithmID;
import iaik.pkcs.pkcs11.Mechanism;
import iaik.pkcs.pkcs11.MechanismInfo;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.objects.Key;
import iaik.pkcs.pkcs11.objects.Object;
import iaik.pkcs.pkcs11.objects.RSAPrivateKey;
import iaik.pkcs.pkcs11.objects.RSAPublicKey;
import iaik.pkcs.pkcs7.DigestInfo;

/**
 * Creates and verifies a signature on a token. The hash is calculated outside
 * the token. Notice that many tokens do not support verification. In this case
 * you will get an exception when the program tries to verify the signature
 * on the token.
 *
 * @author <a href="mailto:Karl.Scheibelhofer@iaik.at"> Karl Scheibelhofer </a>
 * @version 0.1
 * @invariants
 */
public class SignAndVerify {

    static BufferedReader input_;

    static PrintWriter output_;

    static {
        try {
            output_ = new PrintWriter(System.out, true);
            input_ = new BufferedReader(new InputStreamReader(System.in));
        } catch (Throwable thr) {
            thr.printStackTrace();
            output_ = new PrintWriter(System.out, true);
            input_ = new BufferedReader(new InputStreamReader(System.in));
        }
    }

    public static void main(String[] args) {
        if ((args.length != 2) && (args.length != 3)) {
            printUsage();
            System.exit(1);
        }
        try {
            Module pkcs11Module = Module.getInstance(args[0]);
            pkcs11Module.initialize(null);
            Token token = Util.selectToken(pkcs11Module, output_, input_);
            if (token == null) {
                output_.println("We have no token to proceed. Finished.");
                output_.flush();
                System.exit(0);
            }
            Mechanism[] mechanisms = token.getMechanismList();
            Hashtable supportedMechanisms = new Hashtable(mechanisms.length);
            for (int i = 0; i < mechanisms.length; i++) {
                supportedMechanisms.put(mechanisms[i], mechanisms[i]);
            }
            MechanismInfo signatureMechanismInfo;
            if (supportedMechanisms.contains(Mechanism.RSA_PKCS)) {
                signatureMechanismInfo = token.getMechanismInfo(Mechanism.RSA_PKCS);
            } else {
                signatureMechanismInfo = null;
                output_.println("The token does not support mechanism RSA_PKCS. Going to exit.");
                System.exit(0);
            }
            if ((signatureMechanismInfo == null) || !signatureMechanismInfo.isSign()) {
                output_.println("The token does not support signing with mechanism RSA_PKCS. Going to exit.");
                System.exit(0);
            }
            Session session = Util.openAuthorizedSession(token, Token.SessionReadWriteBehavior.RO_SESSION, output_, input_);
            output_.println("################################################################################");
            output_.println("find private signature key");
            RSAPrivateKey templateSignatureKey = new RSAPrivateKey();
            templateSignatureKey.getSign().setBooleanValue(Boolean.TRUE);
            KeyAndCertificate selectedSignatureKeyAndCertificate = Util.selectKeyAndCertificate(session, templateSignatureKey, output_, input_);
            if (selectedSignatureKeyAndCertificate == null) {
                output_.println("We have no signature key to proceed. Finished.");
                output_.flush();
                System.exit(0);
            }
            Key signatureKey = selectedSignatureKeyAndCertificate.getKey();
            output_.println("################################################################################");
            output_.println("################################################################################");
            output_.println("signing data from file: " + args[1]);
            InputStream dataInputStream = new FileInputStream(args[1]);
            MessageDigest digestEngine = MessageDigest.getInstance("SHA-1");
            Mechanism signatureMechanism = Mechanism.RSA_PKCS;
            session.signInit(signatureMechanism, signatureKey);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = dataInputStream.read(dataBuffer)) >= 0) {
                digestEngine.update(dataBuffer, 0, bytesRead);
            }
            byte[] digest = digestEngine.digest();
            DigestInfo digestInfoObject = new DigestInfo(AlgorithmID.sha1, digest);
            byte[] digestInfo = digestInfoObject.toByteArray();
            byte[] signatureValue = session.sign(digestInfo);
            output_.println("The siganture value is: " + new BigInteger(1, signatureValue).toString(16));
            if (args.length == 3) {
                output_.println("Writing signature to file: " + args[2]);
                OutputStream signatureOutput = new FileOutputStream(args[2]);
                signatureOutput.write(signatureValue);
                signatureOutput.flush();
                signatureOutput.close();
            }
            output_.println("################################################################################");
            if ((signatureMechanismInfo == null) || !signatureMechanismInfo.isVerify()) {
                output_.println("The token does not support verification with mechanism RSA_PKCS. Going to exit.");
                System.exit(0);
            }
            boolean verifyInSoftware;
            output_.println("################################################################################");
            output_.println("find public verification key");
            RSAPublicKey templateVerificationKey = new RSAPublicKey();
            templateVerificationKey.getVerify().setBooleanValue(Boolean.TRUE);
            templateVerificationKey.getId().setByteArrayValue(signatureKey.getId().getByteArrayValue());
            session.findObjectsInit(templateVerificationKey);
            Object[] foundVerificationKeyObjects = session.findObjects(1);
            RSAPublicKey verificationKey = null;
            if (foundVerificationKeyObjects.length > 0) {
                verificationKey = (RSAPublicKey) foundVerificationKeyObjects[0];
                output_.println("________________________________________________________________________________");
                output_.println(verificationKey);
                output_.println("________________________________________________________________________________");
                verifyInSoftware = false;
            } else {
                if (selectedSignatureKeyAndCertificate.getCertificate() != null) {
                    output_.println("No matching public key found! Will verify in software.");
                } else {
                    output_.println("No matching public key found and no certificate found! Going to exit.");
                    System.exit(0);
                }
                verifyInSoftware = true;
            }
            session.findObjectsFinal();
            output_.println("################################################################################");
            output_.println("################################################################################");
            if (verifyInSoftware) {
                output_.println("verifying signature in software");
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                byte[] encodedCertificate = selectedSignatureKeyAndCertificate.getCertificate().getValue().getByteArrayValue();
                X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(encodedCertificate));
                Signature signatureEngine = Signature.getInstance("SHA1withRSA");
                signatureEngine.initVerify(certificate.getPublicKey());
                dataInputStream = new FileInputStream(args[1]);
                while ((bytesRead = dataInputStream.read(dataBuffer)) >= 0) {
                    signatureEngine.update(dataBuffer, 0, bytesRead);
                }
                try {
                    if (signatureEngine.verify(signatureValue)) {
                        output_.println("Verified the signature successfully");
                    } else {
                        output_.println("Signature Invalid.");
                    }
                } catch (SignatureException ex) {
                    output_.println("Verification FAILED: " + ex.getMessage());
                }
            } else {
                output_.println("verifying signature on token");
                dataInputStream = new FileInputStream(args[1]);
                while ((bytesRead = dataInputStream.read(dataBuffer)) >= 0) {
                    digestEngine.update(dataBuffer, 0, bytesRead);
                }
                digest = digestEngine.digest();
                digestInfoObject = new DigestInfo(AlgorithmID.sha1, digest);
                digestInfo = digestInfoObject.toByteArray();
                Mechanism verificationMechanism = Mechanism.RSA_PKCS;
                session.verifyInit(verificationMechanism, verificationKey);
                try {
                    session.verify(digestInfo, signatureValue);
                    output_.println("Verified the signature successfully");
                } catch (TokenException ex) {
                    output_.println("Verification FAILED: " + ex.getMessage());
                }
            }
            output_.println("################################################################################");
            session.closeSession();
            pkcs11Module.finalize(null);
        } catch (Throwable thr) {
            thr.printStackTrace();
        } finally {
            output_.close();
        }
    }

    public static void printUsage() {
        output_.println("Usage: SignAndVerify <PKCS#11 module> <file to be signed> [<signature value file>]");
        output_.println(" e.g.: SignAndVerify pk2priv.dll data.dat signature.bin");
        output_.println("The given DLL must be in the search path of the system.");
    }
}
