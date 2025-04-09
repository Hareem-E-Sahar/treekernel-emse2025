package fireteam.security;

import fireteam.interfaces.FxDialog;
import fireteam.orb.client.ClientMain;
import java.awt.Dialog.ModalityType;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import javax.script.ScriptException;

/**
 *
 * @author Tolik1
 */
public class KeystoreWizard extends FxDialog {

    String m_sHost;

    String m_sPort;

    /**
	 * класс для обработки серверного сертификата
	 */
    private class internalTrustManager implements X509TrustManager {

        X509Certificate m_serverCerts[];

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            m_serverCerts = arg0;
        }

        public X509Certificate[] getAcceptedIssuers() {
            return m_serverCerts;
        }
    }

    internalTrustManager m_trustManager = new internalTrustManager();

    public KeystoreWizard() throws ScriptException, ScriptException, NoSuchMethodException, NoSuchMethodException, IllegalAccessException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException, IOException {
        super(ModalityType.APPLICATION_MODAL);
        System.out.println(getClass());
        init(new InputStreamReader(getClass().getResourceAsStream("KeystoreWizardFx.fx")));
    }

    /**
	 * Функцимя получает сертификат сервера, и добавляет его в список доверенных
	 * @param sServer
	 * @param sPort
	 * @return
	 */
    public String[] getServerCertificate(String sServer, String sPort) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLSv1");
            ctx.init(null, new internalTrustManager[] { m_trustManager }, new SecureRandom());
            SSLSocketFactory fac = ctx.getSocketFactory();
            SSLSocket sock = (SSLSocket) fac.createSocket(sServer, Integer.valueOf(sPort));
            sock.startHandshake();
            ArrayList<String> arNames = new ArrayList<String>();
            for (X509Certificate cert : m_trustManager.getAcceptedIssuers()) {
                String sCert = cert.getSubjectX500Principal().getName();
                int Index1 = sCert.indexOf("CN=") + 3;
                int Index2 = sCert.indexOf(",", Index1);
                sCert = sCert.substring(Index1, Index2);
                arNames.add(sCert);
            }
            m_sHost = sServer;
            m_sPort = sPort;
            return arNames.toArray(new String[arNames.size()]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    /**
	 * Функция создает свой сертификат, создает хранилище ключей (без пароля), 
	 * записывает в него сертификат сервера и регистрирует клиентский сертификат на сервере
	 * @param sUserName		- Имя пользователя
	 * @param sKeyName		- Название ключа
	 * @param sKeySize		- Размер ключа
	 * @param sFileName		- Имя файла с ключами
	 * @param sPassword		- Пароль для доступа к закрытому ключу клиегнта
	 * @return успешна или нет операция
	 */
    public boolean createKeyStore(String sUserName, String sKeyName, String sKeySize, String sFileName, String sPassword) {
        try {
            RSA rsa = new RSA();
            rsa.openKeyStore(null, "");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            SecureRandom rand = new SecureRandom((sUserName + sKeyName + sKeySize + sFileName + DateFormat.getDateTimeInstance().format(new Date())).getBytes());
            generator.initialize(new Integer(sKeySize), rand);
            KeyPair keyPair = generator.generateKeyPair();
            for (X509Certificate cert : m_trustManager.getAcceptedIssuers()) {
                String sCert = cert.getSubjectX500Principal().getName();
                int Index1 = sCert.indexOf("CN=") + 3;
                int Index2 = sCert.indexOf(",", Index1);
                sCert = sCert.substring(Index1, Index2);
                rsa.addToKeyStore(sCert, cert);
            }
            X509Certificate cert = rsa.createCertificate(sKeyName, keyPair.getPublic(), keyPair.getPrivate());
            rsa.addToKeyStore(sKeyName, cert, keyPair.getPrivate(), sPassword);
            rsa.store(sFileName);
            return ClientMain.getInstance().registerClient(m_sHost, m_sPort, sUserName, keyPair.getPublic().getEncoded(), sFileName, sPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
