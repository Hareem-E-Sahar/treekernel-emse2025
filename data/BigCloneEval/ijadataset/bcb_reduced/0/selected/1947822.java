package com.festo.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

public class EncryptTool {

    static {
        org.apache.xml.security.Init.init();
    }

    private static Document parseFile(String fileName) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(fileName);
        return document;
    }

    private static SecretKey GenerateKeyEncryptionKey() throws Exception {
        String jceAlgorithmName = "DESede";
        KeyGenerator keyGenerator = KeyGenerator.getInstance(jceAlgorithmName);
        SecretKey keyEncryptKey = keyGenerator.generateKey();
        return keyEncryptKey;
    }

    private static void storeKeyFile(Key keyEncryptKey) throws IOException {
        byte[] keyBytes = keyEncryptKey.getEncoded();
        File keyEncryptKeyFile = new File("keyEncryptKey");
        FileOutputStream outStream = new FileOutputStream(keyEncryptKeyFile);
        outStream.write(keyBytes);
        outStream.close();
        System.out.println("Key encryption key stored in: " + keyEncryptKeyFile.toString());
    }

    private static SecretKey GenerateSymmetricKey() throws Exception {
        String jceAlgorithmName = "AES";
        KeyGenerator keyGenerator = KeyGenerator.getInstance(jceAlgorithmName);
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private static void writeEncryptedDocToFile(Document doc, String fileName) throws Exception {
        File encryptionFile = new File(fileName);
        FileOutputStream outStream = new FileOutputStream(encryptionFile);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outStream);
        transformer.transform(source, result);
        outStream.close();
        encryptionFile.renameTo(new File(fileName.substring(0, fileName.length() - 4) + "_encrypted.xml"));
        System.out.println("Encrypted XML document written to: " + encryptionFile.toString());
    }

    private static boolean fileExists(String uri) {
        return (new File(uri)).exists();
    }

    public static void encrypt(String uri) throws Exception {
        if (fileExists(uri)) {
            Document document = parseFile(uri);
            Key symmetricKey = GenerateSymmetricKey();
            Key keyEncryptKey = GenerateKeyEncryptionKey();
            storeKeyFile(keyEncryptKey);
            XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.TRIPLEDES_KeyWrap);
            keyCipher.init(XMLCipher.WRAP_MODE, keyEncryptKey);
            EncryptedKey encryptedKey = keyCipher.encryptKey(document, symmetricKey);
            Element rootElement = document.getDocumentElement();
            Element elementToEncrypt = rootElement;
            XMLCipher xmlCipher = XMLCipher.getInstance(XMLCipher.AES_128);
            xmlCipher.init(XMLCipher.ENCRYPT_MODE, symmetricKey);
            EncryptedData encryptedDataElement = xmlCipher.getEncryptedData();
            KeyInfo keyInfo = new KeyInfo(document);
            keyInfo.add(encryptedKey);
            encryptedDataElement.setKeyInfo(keyInfo);
            boolean encryptContentsOnly = true;
            xmlCipher.doFinal(document, elementToEncrypt, encryptContentsOnly);
            writeEncryptedDocToFile(document, uri);
        }
    }
}
