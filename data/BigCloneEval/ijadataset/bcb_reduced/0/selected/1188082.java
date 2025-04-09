package org.es.uma.Signer;

import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.w3c.dom.Document;
import iaik.ixsil.core.*;
import iaik.ixsil.keyinfo.*;
import iaik.ixsil.algorithms.*;
import iaik.ixsil.init.*;
import iaik.ixsil.util.*;
import iaik.pkcs.pkcs8.EncryptedPrivateKeyInfo;
import iaik.asn1.structures.AlgorithmID;
import iaik.me.security.rsa.RSAKeyPairGenerator;
import iaik.me.security.*;

/**
 * <p>Title: Gestor de DPDs: CreateSignatureOnKeyInfo</p>
 * <p>Description: Esta clase genera una firma XML cn la librer�a IXSIL. La firma
 * contendr� dos referencias; una apuntando a la DPD que se firma y otra al hijo
 * KeyInfo del elemento Signature. KeyInfo contendr� un hijo KeyValue, especificando
 * la clave p�blica que deber�a usarse para la verificaci�n de la firma via http
 * con el servlet del Servidor de DPDs</p>
 * @author Francisco S�nchez Cid
 * @version 1.0
 */
public class CreateSignatureOnKeyInfo {

    /**
   * The private key to be used for signing.
   */
    protected RSAPrivateKey signerKey_;

    /**
   * The public key which will be integrated in the key information.
   */
    protected RSAPublicKey verifierKey_;

    EncryptedPrivateKeyInfo epki;

    /**
   * Constructor. Inicializa IXSIL y genera las claves privada y p�blica
   * @param args Vector de cadenas en el que se incluye:
   *            1. Path y nombre del fichero de inicializaci�n de la librer�a IXSIL
   *            2. Path del documento XML a firmar
   *            3. Nombre de la DPD en el dominio del servidor
   *            4. Clave secreta del usuario
   * @throws Exception
   */
    public CreateSignatureOnKeyInfo(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("\nPlease specify 4 command line parameter:");
            System.out.println("  (1) Path and filename for the IXSIL library init file");
            System.out.println("  (2) Path and filename for the XML file to sign\n");
            System.out.println("  (3) Name of the DPD in the Server Domain\n");
            System.out.println("  (4) Secret Key of the user. Used to decrypt the private key\n");
        }
        iaik.security.provider.IAIK.addAsJDK14Provider(true);
        URI tempURI = new URI("file", null, args[0], null, null);
        String cada = tempURI.toString();
        IXSILInit.init(new URI("file", null, args[0], null, null));
        RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
        CryptoBag cryptoBag = rsaKeyPairGenerator.generateKeyPair();
        CryptoBag publicKey = cryptoBag.getCryptoBag(cryptoBag.V_KEY_PUBLIC);
        CryptoBag privateKey = cryptoBag.getCryptoBag(cryptoBag.V_KEY_PRIVATE);
        byte[] derEncodedSignerKey = privateKey.getEncoded();
        PKCS8EncodedKeySpec signerKeySpec = new PKCS8EncodedKeySpec(derEncodedSignerKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        signerKey_ = (RSAPrivateKey) keyFactory.generatePrivate(signerKeySpec);
        try {
            epki = new EncryptedPrivateKeyInfo(signerKey_);
            epki.encrypt(args[3], AlgorithmID.pbeWithMD5AndDES_CBC, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] derEncodedVerifierKey = publicKey.getEncoded();
        X509EncodedKeySpec verifierKeySpec = new X509EncodedKeySpec(derEncodedVerifierKey);
        keyFactory = KeyFactory.getInstance("RSA");
        verifierKey_ = (RSAPublicKey) keyFactory.generatePublic(verifierKeySpec);
    }

    /**
   * Crea la firma XML como un documento DOM
   *
   * @param fileToSign Path de la DPD a firmar
   * @param DPDId identificador de la DPD a firmar
   * @param password clave secreta del usuario para desencriptar la clave privada
   *
   * @returns boolean True si todo ha sido correcto. False en caso contrario
   *
   * @throws Exception
   */
    public boolean createSignature(String fileToSign, String soadId, String password) throws Exception {
        String resultingSignatureFile = obtainSignName(fileToSign);
        URI baseURI = new URI("file", null, resultingSignatureFile, null, null);
        Signer signer = new Signer(baseURI);
        SignerSignature signature = signer.getSignature();
        SignerSignedInfo signedInfo = signer.getSignature().getSignerSignedInfo();
        signature.setId(soadId + "Signature");
        Document signatureDOMDoc = signer.toDocument();
        KeyManagerImpl keyManager = new KeyManagerImpl(signatureDOMDoc);
        keyManager.setId("KeyInfo");
        KeyProviderImplKeyValue keyProvider = new KeyProviderImplKeyValue(signatureDOMDoc);
        keyProvider.setVerifierKey(verifierKey_);
        keyManager.addKeyProvider(keyProvider);
        signature.setKeyManager(keyManager);
        CanonicalizationAlgorithmImplCanonicalXML c14nAlg = new CanonicalizationAlgorithmImplCanonicalXML();
        signedInfo.setCanonicalizationAlgorithm(c14nAlg);
        SignatureAlgorithmImplRSA signatureAlg = new SignatureAlgorithmImplRSA();
        RSAPrivateKey signerKeyDecripted = (RSAPrivateKey) epki.decrypt(password);
        signatureAlg.setSignerKey(signerKeyDecripted);
        signedInfo.setSignatureAlgorithm(signatureAlg);
        SignerReference firstRef = signedInfo.createReference();
        SignerReference secondRef = signedInfo.createReference();
        firstRef.setURI(new URI("#KeyInfo"));
        secondRef.setURI(new URI("http://localhost:8080/servlet?userOption=getDpd&fileId=" + soadId));
        DigestAlgorithmImplSHA1 digestAlg1 = new DigestAlgorithmImplSHA1();
        firstRef.setDigestAlgorithm(digestAlg1);
        secondRef.setDigestAlgorithm(digestAlg1);
        signedInfo.addReference(firstRef);
        signedInfo.addReference(secondRef);
        signer.getSignature();
        outputResult(resultingSignatureFile, signer.toDocument());
        return true;
    }

    /**
   * Da formato de salida al resultado de la generaci�n de firma XML
   *
   * @param outputFile El nombre del fichero en el que se escribir� al resultado de la firma XML
   * @param signatureDOMDoc La firma en formato de documento DOM para serializar
   *
   * @throws Exception
   */
    public void outputResult(String outputFile, Document signatureDOMDoc) throws Exception {
        FileOutputStream outputXMLDoc = new FileOutputStream(outputFile);
        DOMUtilsImpl domUtilitites = new DOMUtilsImpl();
        domUtilitites.serializeDocument(signatureDOMDoc, outputXMLDoc);
    }

    /**
   * Pasa la cadena pasada como par�metro a formato URI
   * @param cadToChange cadena origen
   * @return String - cadena resultado
   */
    public String changeToURI(String cadToChange) {
        char oldChar = '\\';
        char newChar = '/';
        String changedCad = cadToChange.replace(oldChar, newChar);
        for (int i = 0; i < changedCad.length(); i++) {
            if (changedCad.charAt(i) == ' ') {
                changedCad = changedCad.substring(0, i) + "%20" + changedCad.substring(i + 1, changedCad.length());
            }
        }
        return changedCad;
    }

    /**
   * Obtiene el nombre del fichero de firma de una DPD a partir del nombre de la propia DPD
   *
   * @param fileToSign nombre del documento a firmar
   *
   * @returns String nombre del fichero que contiene la firma
   */
    public String obtainSignName(String fileToSign) {
        String signName = null;
        int fileNameSize = fileToSign.length();
        String fileSimpleName = fileToSign.substring(0, fileNameSize - 4);
        String fileExtension = fileToSign.substring(fileNameSize - 4, fileNameSize);
        signName = fileSimpleName + "Sign" + fileExtension;
        return signName;
    }
}
