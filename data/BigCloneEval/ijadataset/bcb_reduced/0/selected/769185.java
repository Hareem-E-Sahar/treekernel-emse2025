package fireteam.security;

import sun.security.x509.*;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

/**
 * User: tolik1
 * Date: 24.11.2006
 * Time: 11:13:33
 */
@SuppressWarnings({ "ConstantConditions" })
public final class RSA {

    public static final class ALIAS_LIST {

        public String sAlias;

        public boolean bKey;
    }

    public static final class ES_DATA {

        public byte[] EncData;

        public byte[] Sign;
    }

    private static final String g_keyStoreType = "JKS";

    private static final String g_keySignAlg = "SHA1withRSA";

    private KeyStore m_keyStore = null;

    private KeyStore.PasswordProtection m_keyStorePass;

    private KeyStore.PasswordProtection m_keyPass;

    public void setKeyPass(String sPass) {
        m_keyPass = new KeyStore.PasswordProtection(sPass.toCharArray());
    }

    public String getKeyStorePass() {
        return new String(m_keyStorePass.getPassword());
    }

    @SuppressWarnings("propietary")
    private X509Certificate makeCertificate(X500Name x500name, PrivateKey issuerPrivateKey, PublicKey subjectPublicKey) throws SignatureException, InvalidKeyException, IOException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException {
        long lDays = 180;
        lDays = lDays * 24 * 60 * 60;
        X509CertImpl x509certimpl;
        X500Signer x500signer = getSigner(x500name, issuerPrivateKey);
        Date date = new Date();
        Date date1 = new Date();
        date1.setTime(date1.getTime() + lDays * 1000L);
        CertificateValidity certificatevalidity = new CertificateValidity(date, date1);
        X509CertInfo x509certinfo = new X509CertInfo();
        x509certinfo.set("version", new CertificateVersion(0));
        x509certinfo.set("serialNumber", new CertificateSerialNumber((int) (date.getTime() / 1000L)));
        AlgorithmId algorithmid = x500signer.getAlgorithmId();
        x509certinfo.set("algorithmID", new CertificateAlgorithmId(algorithmid));
        x509certinfo.set("subject", new CertificateSubjectName(x500name));
        x509certinfo.set("key", new CertificateX509Key(subjectPublicKey));
        x509certinfo.set("validity", certificatevalidity);
        x509certinfo.set("issuer", new CertificateIssuerName(x500signer.getSigner()));
        x509certimpl = new X509CertImpl(x509certinfo);
        x509certimpl.sign(issuerPrivateKey, g_keySignAlg);
        return x509certimpl;
    }

    private X500Signer getSigner(X500Name x500name, PrivateKey privateKey) throws InvalidKeyException, NoSuchAlgorithmException {
        Signature signature = Signature.getInstance(g_keySignAlg);
        signature.initSign(privateKey);
        return new X500Signer(signature, x500name);
    }

    /**
	 * Создает сертификат на основе открытого ключа и подписывает его закрытым
	 *
	 * @param sName - Название сертификата (CN)
	 * @param pk	- Открытый ключ
	 * @param sk	- Закрытый ключ для подписи
	 * @return Сертификат
	 * @throws NoSuchProviderException, NoSuchAlgorithmException, IOException, SignatureException, CertificateException, InvalidKeyException - ляляля
	 */
    public X509Certificate createCertificate(String sName, PublicKey pk, PrivateKey sk) throws NoSuchProviderException, NoSuchAlgorithmException, IOException, SignatureException, CertificateException, InvalidKeyException {
        String sStr = "C=\"" + "RU" + "\"";
        sStr += (",ST=\"" + "" + "\"");
        sStr += (",L=\"" + "" + "\"");
        sStr += (",O=\"" + "" + "\"");
        sStr += (",OU=\"" + "" + "\"");
        sStr += (",CN=\"" + sName + "\"");
        sStr += (",emailAddress=\"" + "" + "\"");
        X500Name x500name = new X500Name(sStr);
        return makeCertificate(x500name, sk, pk);
    }

    public void store(String sFilename) throws NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException {
        FileOutputStream out = new FileOutputStream(sFilename);
        m_keyStore.store(out, m_keyStorePass.getPassword());
        out.close();
    }

    /**
	 * Возвращает список ключей в БД
	 *
	 * @return - список
	 * @throws KeyStoreException - ляляля
	 */
    public ArrayList<ALIAS_LIST> listAliases() throws KeyStoreException {
        ArrayList<ALIAS_LIST> array = new ArrayList<ALIAS_LIST>();
        Enumeration Enum = m_keyStore.aliases();
        for (; Enum.hasMoreElements(); ) {
            String alias = (String) Enum.nextElement();
            ALIAS_LIST lst = new ALIAS_LIST();
            lst.sAlias = alias;
            lst.bKey = m_keyStore.isKeyEntry(alias);
            array.add(lst);
        }
        return array;
    }

    /**
	 * Возвращает список закрытых ключей в БД
	 *
	 * @return список
	 * @throws KeyStoreException ляляля
	 */
    public ArrayList<String> listKeyAliases() throws KeyStoreException {
        ArrayList<String> array = new ArrayList<String>();
        Enumeration Enum = m_keyStore.aliases();
        for (; Enum.hasMoreElements(); ) {
            String alias = (String) Enum.nextElement();
            if (m_keyStore.isKeyEntry(alias)) array.add(alias);
        }
        return array;
    }

    /**
	 * Добавляет сертификат в БД
	 *
	 * @param alias - Название
	 * @param cert  - Сертификат
	 * @throws KeyStoreException - ячс
	 */
    public void addToKeyStore(String alias, X509Certificate cert) throws KeyStoreException {
        m_keyStore.setCertificateEntry(alias, cert);
    }

    /**
	 * Добавляет сертификат и закрытый ключ в БД
	 *
	 * @param alias - Название
	 * @param cert  - Сертификат
	 * @param sKey  - Закрытый ключ
	 * @throws KeyStoreException
	 */
    public void addToKeyStore(String alias, X509Certificate cert, PrivateKey sKey, String sPass) throws KeyStoreException {
        X509Certificate[] ax509certificate = new X509Certificate[1];
        ax509certificate[0] = cert;
        m_keyStore.setKeyEntry(alias, sKey, sPass.toCharArray(), ax509certificate);
    }

    public RSA() throws KeyStoreException {
        if (m_keyStore == null) m_keyStore = KeyStore.getInstance(g_keyStoreType);
    }

    @SuppressWarnings({ "EmptyCatchBlock" })
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            m_keyStorePass.destroy();
        } catch (Exception e) {
        }
    }

    /**
	 * Функция открывает существующее или создает новое хранилище ключей
	 *
	 * @param sFileName	- Имя файла
	 * @param keyStorePass - Пароль
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws CertificateException
	 */
    public int openKeyStore(String sFileName, String keyStorePass) throws NoSuchAlgorithmException, IOException, CertificateException {
        m_keyStorePass = new KeyStore.PasswordProtection(keyStorePass.toCharArray());
        if (sFileName == null) m_keyStore.load(null, m_keyStorePass.getPassword()); else {
            FileInputStream fileInputStream = new FileInputStream(sFileName);
            try {
                m_keyStore.load(fileInputStream, m_keyStorePass.getPassword());
            } catch (IOException e) {
                if (e.getCause() instanceof UnrecoverableKeyException) {
                    return 1;
                } else return 2;
            } finally {
                fileInputStream.close();
            }
        }
        return 0;
    }

    /**
	 * Возвращает открытый ключ из БД
	 *
	 * @param sAlias - название
	 * @return открытый ключ
	 * @throws KeyStoreException
	 */
    public PublicKey getPubkey(String sAlias) throws KeyStoreException {
        return m_keyStore.getCertificate(sAlias).getPublicKey();
    }

    /**
	 * Возвращает сертификат из БД по имени
	 *
	 * @param sAlias - Название
	 * @return сертификат
	 * @throws KeyStoreException
	 */
    public X509Certificate getCertificate(String sAlias) throws KeyStoreException {
        return (X509Certificate) m_keyStore.getCertificate(sAlias);
    }

    /**
	 * Возвращает имя сертификата по сертификату
	 *
	 * @param cert - сертификат
	 * @return
	 * @throws KeyStoreException
	 */
    public String findCertificateAlias(X509Certificate cert) throws KeyStoreException {
        return m_keyStore.getCertificateAlias(cert);
    }

    /**
	 * Ищет владельца подписи на сертификате в БД, если нет возвращает null
	 *
	 * @param cert
	 * @return
	 * @throws KeyStoreException
	 */
    @SuppressWarnings({ "EmptyCatchBlock" })
    public String findCertificateSigner(X509Certificate cert) throws KeyStoreException {
        Enumeration Enum = m_keyStore.aliases();
        for (; Enum.hasMoreElements(); ) {
            String alias = (String) Enum.nextElement();
            PublicKey pKey = m_keyStore.getCertificate(alias).getPublicKey();
            try {
                cert.verify(pKey);
                return alias;
            } catch (Exception e) {
            }
        }
        return "No Signer";
    }

    /**
	 * Удаляет ключ из базы
	 *
	 * @param sAlias - Название
	 * @throws KeyStoreException
	 */
    public void deleteEntry(String sAlias) throws KeyStoreException {
        m_keyStore.deleteEntry(sAlias);
    }

    /**
	 * Возвращает закрытый ключ по имени и паролю
	 *
	 * @param sAlias	- название
	 * @param sPassword - пароль
	 * @return
	 */
    public PrivateKey getSeckey(String sAlias, String sPassword) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        char[] keyStorePassInChars = sPassword.toCharArray();
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) m_keyStore.getEntry(sAlias, new KeyStore.PasswordProtection(keyStorePassInChars));
        return pkEntry.getPrivateKey();
    }

    public KeyStore.PrivateKeyEntry getEntry(String sAlias, String sPassword) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        char[] keyStorePassInChars = sPassword.toCharArray();
        return (KeyStore.PrivateKeyEntry) m_keyStore.getEntry(sAlias, new KeyStore.PasswordProtection(keyStorePassInChars));
    }

    /**
	 * Возвращает открытый ключ
	 *
	 * @param pkData - данные
	 * @return
	 */
    public static PublicKey getPubkey(byte[] pkData) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pkData);
        System.out.println(pubKeySpec.getFormat());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(pubKeySpec);
    }

    /**
	 * Возвращает закрытый ключ из данных
	 *
	 * @param pkData - данные
	 * @return
	 */
    public static PrivateKey getSeckey(byte[] pkData) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec secKeySpec = new PKCS8EncodedKeySpec(pkData);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(secKeySpec);
    }

    /**
	 * Возвращает подпись к данным и ключем
	 *
	 * @param pk   - закрытый ключ
	 * @param data - подписываемые данные
	 * @return
	 */
    public static byte[] signData(PrivateKey pk, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsasig = Signature.getInstance("SHA1withRSA");
        rsasig.initSign(pk);
        rsasig.update(data);
        return rsasig.sign();
    }

    /**
	 * Проверяет соответствие подписи данным и ключу
	 *
	 * @param data	 - Исходные даннвц
	 * @param SignData - Подпись
	 * @param pk	   - Открытый ключ для сравнения
	 * @return
	 */
    public static boolean signVerify(byte[] data, byte[] SignData, PublicKey pk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsasig = Signature.getInstance("SHA1withRSA");
        rsasig.initVerify(pk);
        rsasig.update(data);
        return rsasig.verify(SignData);
    }

    /**
	 * Функция шифрует данные из потока с помощью открытого ключа
	 *
	 * @param is - Входной поток с данными
	 * @param pk - Открытый ключ
	 * @return Шифрованные данные
	 */
    private static byte[] encrypt(InputStream is, PublicKey pk) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
        Cipher cip = Cipher.getInstance("RSA");
        cip.init(Cipher.ENCRYPT_MODE, pk);
        RSAPublicKey rpk = (RSAPublicKey) pk;
        int buLen = rpk.getModulus().bitLength() / 8 - 11;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte data[] = new byte[buLen];
        while (is.available() > 0) {
            int size = is.read(data);
            bo.write(cip.doFinal(data, 0, size));
        }
        bo.close();
        return bo.toByteArray();
    }

    /**
	 * Функция расшифровывает данные с помощью закрытого ключа
	 *
	 * @param is - Входной поток с шифрованными данными
	 * @param sk - Закрытый ключ
	 * @return расшифрованные данные
	 */
    public static byte[] decrypt(InputStream is, PrivateKey sk) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
        Cipher cip = Cipher.getInstance("RSA");
        cip.init(Cipher.DECRYPT_MODE, sk);
        RSAPrivateKey rpk = (RSAPrivateKey) sk;
        int buLen = rpk.getModulus().bitLength() / 8 - 11;
        int len = cip.getOutputSize(buLen);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte data[] = new byte[len];
        while (is.available() > 0) {
            int size = is.read(data);
            bo.write(cip.doFinal(data, 0, size));
        }
        bo.close();
        return bo.toByteArray();
    }

    public ES_DATA encryptSign(Object obj, String sSeckey, String sPwd, String sPubkey) throws IOException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, SignatureException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        oo.close();
        bo.close();
        byte ud[] = bo.toByteArray();
        byte sign[] = RSA.signData(getSeckey(sSeckey, sPwd), ud);
        byte data[] = RSA.encrypt(new ByteArrayInputStream(ud), getPubkey(sPubkey));
        ES_DATA es = new ES_DATA();
        es.Sign = sign;
        es.EncData = data;
        return es;
    }

    public ES_DATA encryptSign(String obj, String sSeckey, String sPwd, String sPubkey) throws IOException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, SignatureException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeUTF(obj);
        oo.close();
        bo.close();
        byte ud[] = bo.toByteArray();
        byte sign[] = RSA.signData(getSeckey(sSeckey, sPwd), ud);
        byte data[] = RSA.encrypt(new ByteArrayInputStream(ud), getPubkey(sPubkey));
        ES_DATA es = new ES_DATA();
        es.Sign = sign;
        es.EncData = data;
        return es;
    }

    public static byte[] encryptSign(Object obj, PrivateKey sKey, PublicKey pKey) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        oo.close();
        bo.close();
        byte ud[] = bo.toByteArray();
        byte sign[] = RSA.signData(sKey, ud);
        bo = new ByteArrayOutputStream();
        oo = new ObjectOutputStream(bo);
        oo.writeObject(ud);
        oo.writeObject(sign);
        oo.close();
        bo.close();
        return RSA.encrypt(new ByteArrayInputStream(bo.toByteArray()), pKey);
    }

    public boolean addTrustedCert(String alias, InputStream inputstream) {
        X509Certificate x509certificate;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            x509certificate = (X509Certificate) cf.generateCertificate(inputstream);
            addToKeyStore(alias, x509certificate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private KeyManager[] getKeyManagers() {
        try {
            String keyManagerAlgorithm = "SunX509";
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(keyManagerAlgorithm);
            keyManagerFactory.init(m_keyStore, m_keyPass.getPassword());
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private TrustManager[] getTrustManagers() {
        try {
            String trustManagerAlgorithm = "SunX509";
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerAlgorithm);
            trustManagerFactory.init(m_keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private SecureRandom getSecureRandom() {
        try {
            String secureRandomAlgorithm = "SHA1PRNG";
            return SecureRandom.getInstance(secureRandomAlgorithm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private SSLContext createSSLContext() {
        try {
            String sslContextProtocol = "TLSv1";
            SSLContext ctx = SSLContext.getInstance(sslContextProtocol);
            ctx.init(getKeyManagers(), getTrustManagers(), getSecureRandom());
            return ctx;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private SSLContext createClientSSLContext() {
        try {
            String sslContextProtocol = "TLSv1";
            SSLContext ctx = SSLContext.getInstance(sslContextProtocol);
            ctx.init(null, getTrustManagers(), getSecureRandom());
            return ctx;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SSLSocketFactory getSocketFactory() {
        return createClientSSLContext().getSocketFactory();
    }

    public SSLServerSocketFactory getServerSocketFactory() {
        return createSSLContext().getServerSocketFactory();
    }
}
