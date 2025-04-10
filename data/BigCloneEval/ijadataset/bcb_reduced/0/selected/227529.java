package com.ekeyman.client.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ekeyman.client.service.EkeymanService;
import com.ekeymanlib.dto.AppDeviceApiKeys;
import com.ekeymanlib.dto.EncryptionKeys;
import com.ekeymanlib.dto.VendorProfile;

public class EkeyManWsPbeFileUT {

    private static final String[] SPRING_CONFIG_FILES = new String[] { "applicationContext.xml" };

    private EkeymanService ekeymanService;

    ClassPathXmlApplicationContext ctx;

    @Test
    public void testEnd2End() {
        String openidurl = "http://my.yahoo.com/openid/jimmy110/";
        String apiKey = "";
        String emailAddress = "james.bell01@estateplusplus.com";
        String application = "HighlySecureAndroidFinancialApp";
        String device = "704-942-7257";
        String resourceUri = "clearText-5wishes.pdf";
        System.out.println("Entered testEnd2End...");
        if (apiKey == null || apiKey.equalsIgnoreCase("")) {
            apiKey = getEkeymanService().getUser(openidurl);
            if (apiKey == null || apiKey.equalsIgnoreCase("")) {
                apiKey = getEkeymanService().registerVendor("Jimmy Bell", "4339 Castleton Road", "Charlotte", "NC", "28211", "704-942-7257", emailAddress);
                getEkeymanService().registerUser(apiKey, openidurl);
            }
        }
        VendorProfile vendorProfile = getEkeymanService().getVendorProfile(apiKey);
        assert vendorProfile != null : "Expected vendorProfile to be not null";
        AppDeviceApiKeys appDeviceApiKeys = getEkeymanService().registerAppDevice(apiKey, application, device);
        String location = "35.236,-17.887";
        int ivLength = 16;
        int saltLength = 32;
        int iterationCountMax = 2048;
        String passphrase = "1lannin2!";
        System.out.println("passphrase: " + passphrase);
        try {
            EncryptionKeys keysToEncrypt = getEkeymanService().createEncryptionKeys(resourceUri, appDeviceApiKeys.getPublicKey(), appDeviceApiKeys.getPrivateKey(), 16, ivLength, saltLength, iterationCountMax, location);
            SecureRandom random = new SecureRandom();
            String secretKeyType = "PBEWITHSHA256AND256BITAES-CBC-BC";
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(secretKeyType);
            PBEKeySpec eKeySpec = new PBEKeySpec(passphrase.toCharArray(), keysToEncrypt.getSaltBytes(), keysToEncrypt.getIterationCount(), 1024);
            SecretKey eKey = keyFactory.generateSecret(eKeySpec);
            Cipher eCipher = Cipher.getInstance("AES/CTR/NOPADDING");
            IvParameterSpec eIvParameterSpec = new IvParameterSpec(keysToEncrypt.getIvBytes());
            eCipher.init(Cipher.ENCRYPT_MODE, eKey, eIvParameterSpec, random);
            InputStream clearTextFis = new FileInputStream(new File("C:/tmp/clearText-5wishes.pdf"));
            OutputStream cipherTextFos = new FileOutputStream(new File("C:/tmp/cipherText-5wishes.pdf"));
            cipherTextFos = new CipherOutputStream(cipherTextFos, eCipher);
            byte[] ebuf = new byte[4096];
            int enumRead = 0;
            while ((enumRead = clearTextFis.read(ebuf)) >= 0) {
                cipherTextFos.write(ebuf, 0, enumRead);
            }
            cipherTextFos.close();
            EncryptionKeys keysToDecrypt = getEkeymanService().getEncryptionKeys(resourceUri, appDeviceApiKeys.getPublicKey(), appDeviceApiKeys.getPrivateKey(), location);
            PBEKeySpec dKeySpec = new PBEKeySpec(passphrase.toCharArray(), keysToDecrypt.getSaltBytes(), keysToDecrypt.getIterationCount(), 1024);
            SecretKey dKey = keyFactory.generateSecret(dKeySpec);
            Cipher dCipher = Cipher.getInstance("AES/CTR/NOPADDING");
            IvParameterSpec dIvParameterSpec = new IvParameterSpec(keysToDecrypt.getIvBytes());
            dCipher.init(Cipher.DECRYPT_MODE, dKey, dIvParameterSpec, random);
            InputStream cipherTextFis = new FileInputStream(new File("C:/tmp/cipherText-5wishes.pdf"));
            OutputStream decryptedTextFos = new FileOutputStream(new File("C:/tmp/decryptedText-5wishes.pdf"));
            cipherTextFis = new CipherInputStream(cipherTextFis, dCipher);
            int dnumRead = 0;
            byte[] dbuf = new byte[4096];
            while ((dnumRead = cipherTextFis.read(dbuf)) >= 0) {
                decryptedTextFos.write(dbuf, 0, dnumRead);
            }
            decryptedTextFos.close();
            getEkeymanService().deleteVendor(apiKey);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Exited testEnd2End...");
    }

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("Before suite...");
        ctx = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILES);
        setEkeymanService((EkeymanService) ctx.getBeanFactory().getBean("serviceClient"));
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("After suite...");
    }

    public void setEkeymanService(EkeymanService ekeymanService) {
        this.ekeymanService = ekeymanService;
    }

    public EkeymanService getEkeymanService() {
        return ekeymanService;
    }
}
