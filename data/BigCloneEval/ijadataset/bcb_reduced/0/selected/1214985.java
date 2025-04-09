package br.net.woodstock.rockframework.security.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Hashtable;
import junit.framework.TestCase;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import br.net.woodstock.rockframework.security.Alias;
import br.net.woodstock.rockframework.security.cert.KeyUsageType;
import br.net.woodstock.rockframework.security.cert.PrivateKeyHolder;
import br.net.woodstock.rockframework.security.cert.impl.BouncyCastleCertificateBuilder;
import br.net.woodstock.rockframework.security.crypt.KeyPairType;
import br.net.woodstock.rockframework.security.sign.PKCS7SignatureParameters;
import br.net.woodstock.rockframework.security.sign.PKCS7Signer;
import br.net.woodstock.rockframework.security.sign.Signatory;
import br.net.woodstock.rockframework.security.sign.Signature;
import br.net.woodstock.rockframework.security.sign.SignatureType;
import br.net.woodstock.rockframework.security.sign.impl.AsStringSigner;
import br.net.woodstock.rockframework.security.sign.impl.Base64Signer;
import br.net.woodstock.rockframework.security.sign.impl.BouncyCastlePKCS7Signer;
import br.net.woodstock.rockframework.security.sign.impl.HexSigner;
import br.net.woodstock.rockframework.security.sign.impl.KeyPairSigner;
import br.net.woodstock.rockframework.security.sign.impl.PDFSigner;
import br.net.woodstock.rockframework.security.store.KeyStoreType;
import br.net.woodstock.rockframework.security.store.PasswordAlias;
import br.net.woodstock.rockframework.security.store.impl.JCAStore;
import br.net.woodstock.rockframework.security.timestamp.TimeStamp;
import br.net.woodstock.rockframework.security.timestamp.TimeStampClient;
import br.net.woodstock.rockframework.security.timestamp.impl.URLTimeStampClient;
import br.net.woodstock.rockframework.utils.Base64Utils;
import br.net.woodstock.rockframework.utils.IOUtils;

public class SignerTest extends TestCase {

    public static final String[] FREE_TSA = new String[] { "http://tsa.safelayer.com:8093", "https://tsa.aloaha.com/tsa.asp", "http://dse200.ncipher.com/TSS/HttpTspServer", "http://ca.signfiles.com/TSAServer.aspx" };

    public static final TimeStampClient TSA_CLIENT_STF = new STFTimeStampClient("201.49.148.134", 318);

    static {
        System.setProperty("http.proxyHost", "10.30.1.12");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
        System.setProperty("sun.net.client.defaultReadTimeout", "5000");
    }

    public void xtest1() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyPairType.RSA.getAlgorithm());
        KeyPair keyPair = generator.generateKeyPair();
        AsStringSigner signer = new AsStringSigner(new Base64Signer(new KeyPairSigner(keyPair, SignatureType.SHA1_RSA)));
        FileInputStream inputStream = new FileInputStream("/home/lourival/.m2/repository/br/net/woodstock/rockframework/rockframework-core/1.2.2/rockframework-core-1.2.2.jar");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        String sign = signer.signAsString(bytes);
        System.out.println(sign);
        boolean ok = signer.verifyAsString(bytes, sign);
        System.out.println(ok);
        inputStream.close();
    }

    public void xtest2() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyPairType.RSA.getAlgorithm());
        KeyPair keyPair = generator.generateKeyPair();
        AsStringSigner signer = new AsStringSigner(new HexSigner(new KeyPairSigner(keyPair, SignatureType.SHA1_RSA)));
        FileInputStream inputStream = new FileInputStream("/home/lourival/.m2/repository/br/net/woodstock/rockframework/rockframework-core/1.2.2/rockframework-core-1.2.2.jar");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        String sign = signer.signAsString(bytes);
        System.out.println(sign);
        boolean ok = signer.verifyAsString(bytes, sign);
        System.out.println(ok);
        inputStream.close();
    }

    public void xtest3() throws Exception {
        FileInputStream fileInputStream = new FileInputStream("/home/lourival/Documentos/curriculum.pdf");
        byte[] pdf = IOUtils.toByteArray(fileInputStream);
        fileInputStream.close();
        BouncyCastleCertificateBuilder builder1 = new BouncyCastleCertificateBuilder("Lourival Sabino 1");
        builder1.withIssuer("Woodstock Tecnologia 1");
        builder1.withV3Extensions(true);
        builder1.withKeyUsage(KeyUsageType.DIGITAL_SIGNATURE, KeyUsageType.NON_REPUDIATION, KeyUsageType.KEY_AGREEMENT);
        PrivateKeyHolder holder1 = builder1.build();
        TimeStampClient timeStampClient = new URLTimeStampClient("http://tsa.safelayer.com:8093");
        PKCS7SignatureParameters signerInfo = new PKCS7SignatureParameters(holder1);
        signerInfo.setContactInfo("ConcactInfo");
        signerInfo.setLocation("Location");
        signerInfo.setName("Lourival Sabino");
        signerInfo.setReason("Reason");
        signerInfo.setTimeStampClient(timeStampClient);
        PKCS7Signer signer = new BouncyCastlePKCS7Signer(signerInfo);
        byte[] signed = signer.sign(pdf);
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/sign.p7b");
        fileOutputStream.write(signed);
        fileOutputStream.close();
        System.out.println(signer.verify(pdf, signed));
    }

    public void xtest4() throws Exception {
        byte[] pdf = "Lourival Sabino da Silva J�nior".getBytes();
        BouncyCastleCertificateBuilder builder1 = new BouncyCastleCertificateBuilder("Lourival Sabino 1");
        builder1.withIssuer("Woodstock Tecnologia 1");
        builder1.withV3Extensions(true);
        builder1.withKeyUsage(KeyUsageType.DIGITAL_SIGNATURE, KeyUsageType.NON_REPUDIATION, KeyUsageType.KEY_AGREEMENT);
        PrivateKeyHolder holder1 = builder1.build();
        URLTimeStampClient timeStampClient = new URLTimeStampClient("http://tsa.safelayer.com:8093");
        timeStampClient.setDebug(true);
        PKCS7SignatureParameters signerInfo = new PKCS7SignatureParameters(holder1);
        signerInfo.setTimeStampClient(timeStampClient);
        PKCS7Signer signer = new BouncyCastlePKCS7Signer(signerInfo);
        byte[] signed = signer.sign(pdf);
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/sign.pdf.p7s");
        fileOutputStream.write(signed);
        fileOutputStream.close();
    }

    public void xtest5() throws Exception {
        FileInputStream fileInputStream = new FileInputStream("/tmp/sign.pdf");
        byte[] pdf = IOUtils.toByteArray(fileInputStream);
        fileInputStream.close();
        BouncyCastleCertificateBuilder builder = new BouncyCastleCertificateBuilder("Lourival Sabino 1");
        builder.withIssuer("Woodstock Tecnologia 1");
        builder.withV3Extensions(true);
        builder.withKeyUsage(KeyUsageType.DIGITAL_SIGNATURE, KeyUsageType.NON_REPUDIATION, KeyUsageType.KEY_AGREEMENT);
        PrivateKeyHolder holder = builder.build();
        TimeStampClient timeStampClient = null;
        PKCS7SignatureParameters signerInfo = new PKCS7SignatureParameters(holder);
        signerInfo.setContactInfo("ConcactInfo");
        signerInfo.setLocation("Location");
        signerInfo.setName("Lourival Sabino");
        signerInfo.setReason("Reason");
        signerInfo.setTimeStampClient(timeStampClient);
        PDFSigner signer = new PDFSigner(signerInfo);
        byte[] signed = signer.sign(pdf);
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/sign2.pdf");
        fileOutputStream.write(signed);
        fileOutputStream.close();
        Signature[] signatures = signer.getSignatures(signed);
        for (Signature s : signatures) {
            System.out.println(s.getLocation());
            for (Signatory ss : s.getSignatories()) {
                System.out.println("\t" + ss.getIssuer());
                System.out.println("\t" + ss.getSubject());
            }
        }
    }

    public void test5x1() throws Exception {
        PDFSigner signer = new PDFSigner(null);
        byte[] signed = IOUtils.toByteArray(new FileInputStream("/tmp/sign2.pdf"));
        Signature[] signatures = signer.getSignatures(signed);
        for (Signature s : signatures) {
            System.out.println(s.getLocation());
            for (Signatory ss : s.getSignatories()) {
                System.out.println("\t" + ss.getIssuer());
                System.out.println("\t" + ss.getSubject());
            }
        }
    }

    public void xtest6() throws Exception {
        BouncyCastleCertificateBuilder builder = new BouncyCastleCertificateBuilder("Lourival Sabino 1");
        builder.withIssuer("TSE");
        builder.withV3Extensions(true);
        builder.withKeyUsage(KeyUsageType.DIGITAL_SIGNATURE, KeyUsageType.NON_REPUDIATION, KeyUsageType.KEY_AGREEMENT);
        PrivateKeyHolder holder = builder.build();
        FileInputStream fileInputStream = new FileInputStream("/home/lourival/Documentos/curriculum.pdf");
        TimeStampClient timeStampClient = TSA_CLIENT_STF;
        PKCS7SignatureParameters signerInfo = new PKCS7SignatureParameters(holder);
        signerInfo.setContactInfo("ConcactInfo");
        signerInfo.setLocation("Location");
        signerInfo.setName("Lourival Sabino");
        signerInfo.setReason("Reason");
        signerInfo.setTimeStampClient(timeStampClient);
        PDFSigner signer = new PDFSigner(signerInfo);
        byte[] signed = signer.sign(IOUtils.toByteArray(fileInputStream));
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/sign.pdf");
        fileOutputStream.write(signed);
        fileInputStream.close();
        fileOutputStream.close();
    }

    public void xtest6x1() throws Exception {
        BouncyCastleCertificateBuilder builder = new BouncyCastleCertificateBuilder("Lourival Sabino 2");
        builder.withIssuer("TSE");
        builder.withV3Extensions(true);
        builder.withKeyUsage(KeyUsageType.DIGITAL_SIGNATURE, KeyUsageType.NON_REPUDIATION, KeyUsageType.KEY_AGREEMENT);
        PrivateKeyHolder holder = builder.build();
        FileInputStream fileInputStream = new FileInputStream("/tmp/sign.pdf");
        TimeStampClient timeStampClient = TSA_CLIENT_STF;
        PKCS7SignatureParameters signerInfo = new PKCS7SignatureParameters(holder);
        signerInfo.setContactInfo("ConcactInfo");
        signerInfo.setLocation("Location");
        signerInfo.setName("Lourival Sabino");
        signerInfo.setReason("Reason");
        signerInfo.setTimeStampClient(timeStampClient);
        PDFSigner signer = new PDFSigner(signerInfo);
        byte[] signed = signer.sign(IOUtils.toByteArray(fileInputStream));
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/sign2.pdf");
        fileOutputStream.write(signed);
        fileInputStream.close();
        fileOutputStream.close();
    }

    public void xtest7() throws Exception {
        JCAStore store = new JCAStore(KeyStoreType.JKS);
        store.read(new FileInputStream("/home/lourival/Downloads/LOURIVALSABINO.jks"), "storepasswd");
        FileInputStream fileInputStream = new FileInputStream("/home/lourival/Documentos/teste.pdf");
        TimeStampClient timeStampClient = new URLTimeStampClient("http://tsa.safelayer.com:8093");
        PKCS7SignatureParameters signerInfo = new PKCS7SignatureParameters(new Alias[] { new PasswordAlias("lourival sabino", "lourival") }, store);
        signerInfo.setContactInfo("ConcactInfo");
        signerInfo.setLocation("Location");
        signerInfo.setName("Lourival Sabino");
        signerInfo.setReason("Reason");
        signerInfo.setTimeStampClient(timeStampClient);
        PDFSigner signer = new PDFSigner(signerInfo);
        byte[] signed = signer.sign(IOUtils.toByteArray(fileInputStream));
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/teste-demo.pdf");
        fileOutputStream.write(signed);
        fileInputStream.close();
        fileOutputStream.close();
    }

    public void xtest8() throws Exception {
        PDFSigner signer = new PDFSigner(null);
        FileInputStream inputStream = new FileInputStream("/tmp/sign2.pdf");
        Signature[] signatures = signer.getSignatures(IOUtils.toByteArray(inputStream));
        for (Signature signature : signatures) {
            System.out.println(signature.getLocation());
            for (Signatory signatory : signature.getSignatories()) {
                System.out.println("\tSubject: " + signatory.getSubject());
                System.out.println("\tIssuer : " + signatory.getIssuer());
            }
            TimeStamp timeStamp = signature.getTimeStamp();
            if (timeStamp != null) {
                System.out.println("Salvando o timeStamp");
                FileOutputStream outputStream = new FileOutputStream("/tmp/teste-demo.pdf.p7m");
                outputStream.write(timeStamp.getEncoded());
                outputStream.close();
            }
            System.out.println("Salvando a assinatura");
            FileOutputStream outputStream = new FileOutputStream("/tmp/teste-demo.pdf.p7s");
            outputStream.write(signature.getEncoded());
            outputStream.close();
        }
        inputStream.close();
    }

    public void xtest9() throws Exception {
        JCAStore store = new JCAStore(KeyStoreType.JKS);
        store.read(new FileInputStream("/home/lourival/Downloads/LOURIVALSABINO.jks"), "storepasswd");
        FileInputStream fileInputStream = new FileInputStream("/home/lourival/Documentos/teste.pdf");
        TimeStampClient timeStampClient = new URLTimeStampClient("http://localhost:18080/carimbador-web/carimbador");
        PKCS7SignatureParameters signerInfo = new PKCS7SignatureParameters(new Alias[] { new PasswordAlias("lourival sabino", "lourival") }, store);
        signerInfo.setContactInfo("ConcactInfo");
        signerInfo.setLocation("Location");
        signerInfo.setName("Lourival Sabino");
        signerInfo.setReason("Reason");
        signerInfo.setTimeStampClient(timeStampClient);
        PDFSigner signer = new PDFSigner(signerInfo);
        byte[] signed = signer.sign(IOUtils.toByteArray(fileInputStream));
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/teste-demo-local.pdf");
        fileOutputStream.write(signed);
        fileInputStream.close();
        fileOutputStream.close();
    }

    public void xtest10() throws Exception {
        FileInputStream fileOutputStream = new FileInputStream("/tmp/sign.pdf.p7s");
        byte[] data = IOUtils.toByteArray(fileOutputStream);
        CMSSignedData signedData = new CMSSignedData(data);
        System.out.println(signedData.getSignedContentTypeOID());
        byte[] content = (byte[]) signedData.getSignedContent().getContent();
        System.out.println(data.length);
        System.out.println(new String(content));
        SignerInformationStore signerInformationStore = signedData.getSignerInfos();
        SignerInformation signerInformation = (SignerInformation) signerInformationStore.getSigners().iterator().next();
        System.out.println("Signed");
        Hashtable signedTable = signerInformation.getSignedAttributes().toHashtable();
        for (Object o : signedTable.keySet()) {
            ASN1ObjectIdentifier identifier = (ASN1ObjectIdentifier) o;
            Attribute attribute = (Attribute) signedTable.get(o);
            System.out.println(identifier + " => " + attribute.getAttrType() + " => " + attribute.getAttrValues());
        }
        System.out.println("Unsigned");
        Hashtable unsignedTable = signerInformation.getUnsignedAttributes().toHashtable();
        for (Object o : unsignedTable.keySet()) {
            ASN1ObjectIdentifier identifier = (ASN1ObjectIdentifier) o;
            Attribute attribute = (Attribute) signedTable.get(o);
            System.out.println(identifier + " => " + attribute);
            System.out.println(identifier + " => " + signerInformation.getUnsignedAttributes().get(identifier).getAttrType());
            System.out.println(identifier + " => " + signerInformation.getUnsignedAttributes().get(identifier).getDERObject());
            System.out.println(identifier + " => " + signerInformation.getUnsignedAttributes().getAll(identifier));
        }
        if (unsignedTable.containsKey(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken)) {
            DERSequence derSequence = (DERSequence) signerInformation.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken).getDERObject();
            System.out.println("TOKEN: " + derSequence.getEncoded().length);
            for (int i = 0; i < derSequence.size(); i++) {
                DERObject derObject = (DERObject) derSequence.getObjectAt(i);
                System.out.println(new String(Base64Utils.toBase64(derObject.getEncoded())));
            }
        }
        System.out.println("Token Original");
        TimeStampResponse timeStampResponse = new TimeStampResponse(IOUtils.toByteArray(new FileInputStream("/tmp/tsa-client-1332419788210WnRO.tsr")));
        TimeStampToken timeStampToken = timeStampResponse.getTimeStampToken();
        System.out.println(new String(Base64Utils.toBase64(timeStampToken.getEncoded())));
    }
}
