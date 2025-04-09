import java.util.*;
import java.math.BigInteger;
import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import javax.security.auth.x500.*;
import org.bouncycastle.jce.provider.*;

public class iSecurityManager extends Thread {

    public iSecManagerUI ui;

    public Object lock = new Object();

    private SSLSocketFactory socketFactory;

    private int ISECURITYMANAGER_PORT = 2000;

    private String ISECURITYMANAGER_KEYSTORE = "iSecurityManager";

    private char[] ISECURITYMANAGER_KEYSTORE_PASSWORD = "iSec123".toCharArray();

    private char[] ISECURITYMANAGER_KEY_PASSWORD = "iSec123".toCharArray();

    private String USER_NAME;

    private String USER_LOGIN;

    private String USER_PASSWORD;

    private String ISIGN_HOSTNAME;

    private int ISIGN_PORT = 4000;

    private ServerSocket _serverSocket = null;

    private KeyStore serverKeyStore = null;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    private java.security.cert.Certificate[] loginCert;

    public iSecurityManager(iSecManagerUI ui) {
        _serverSocket = null;
        this.ui = ui;
    }

    public void run() {
        try {
            _serverSocket = new ServerSocket(this.ISECURITYMANAGER_PORT);
            serverKeyStore = KeyStore.getInstance("JKS");
            serverKeyStore.load(new FileInputStream(this.ISECURITYMANAGER_KEYSTORE + ".ks"), ISECURITYMANAGER_KEYSTORE_PASSWORD);
            KeyPairGenerator kg = KeyPairGenerator.getInstance("DSA");
            kg.initialize(1024);
            KeyPair kp = kg.generateKeyPair();
            privateKey = kp.getPrivate();
            publicKey = kp.getPublic();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(serverKeyStore, ISECURITYMANAGER_KEYSTORE_PASSWORD);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(serverKeyStore);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            socketFactory = sslContext.getSocketFactory();
            while (true) {
                Socket socket = _serverSocket.accept();
                if (socket.getInetAddress().equals(socket.getLocalAddress())) {
                    iSecurityManagerThread thread = new iSecurityManagerThread(this, socket);
                    thread.start();
                } else {
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not listen on port: " + ISECURITYMANAGER_PORT);
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean contactISIGN(String login, String pwd, String hostname) {
        this.USER_LOGIN = login;
        this.USER_PASSWORD = pwd;
        this.ISIGN_HOSTNAME = hostname;
        try {
            synchronized (lock) {
                SSLSocket sock = (SSLSocket) socketFactory.createSocket(ISIGN_HOSTNAME, ISIGN_PORT);
                SSLSession sslSession = sock.getSession();
                HandshakeCompletedEvent event = new HandshakeCompletedEvent(sock, sslSession);
                java.security.cert.Certificate[] certificates = event.getPeerCertificates();
                iSignLogin loginObject = new iSignLogin(USER_LOGIN, USER_PASSWORD, publicKey);
                (new ObjectOutputStream(sock.getOutputStream())).writeObject(loginObject);
                ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
                Object receivedObject;
                while ((receivedObject = ois.readObject()) != null) {
                    if (receivedObject instanceof java.security.cert.Certificate[]) {
                        loginCert = (java.security.cert.Certificate[]) receivedObject;
                        ui.statusUpdate("iSIGNed certificate received!");
                        USER_NAME = ((X509Certificate) loginCert[0]).getSubjectDN().toString();
                        USER_NAME = USER_NAME.substring(3, USER_NAME.indexOf(','));
                        break;
                    } else if (receivedObject instanceof String) {
                        ui.statusUpdate((String) receivedObject);
                        return false;
                    }
                }
                sock.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        try {
            loginCert = null;
        } catch (Exception e) {
        }
    }

    public KeyStore getServerKeyStore() {
        return serverKeyStore;
    }

    public java.security.cert.Certificate[] getCert() {
        return loginCert;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getUserName() {
        return USER_NAME;
    }
}
