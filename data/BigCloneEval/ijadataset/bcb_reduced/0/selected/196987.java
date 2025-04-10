package org.metastatic.rsync.v2;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLUtil {

    private SSLUtil() {
    }

    public static Socket getSSLSocket(String remoteHost, int remotePort) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket socket = factory.createSocket(remoteHost, remotePort);
        ((SSLSocket) socket).startHandshake();
        return socket;
    }

    public static ServerSocketChannel getSSLServerSocketChannel(String keystore, Secrets secrets) throws CertificateException, KeyManagementException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        ServerSocketFactory f = getSSLServerSocketFactory(keystore, secrets);
        return f.createServerSocket().getChannel();
    }

    public static ServerSocket getSSLServerSocket(int port, String keystore, Secrets secrets) throws CertificateException, KeyManagementException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return getSSLServerSocketFactory(keystore, secrets).createServerSocket(port);
    }

    public static ServerSocket getSSLServerSocket(int port, InetAddress addr, String keystore, Secrets secrets) throws CertificateException, KeyManagementException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return getSSLServerSocketFactory(keystore, secrets).createServerSocket(port, 0, addr);
    }

    private static ServerSocketFactory getSSLServerSocketFactory(String keystore, Secrets secrets) throws CertificateException, KeyManagementException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore ks = null;
        if (keystore != null) {
            String type = null;
            int i;
            if ((i = keystore.indexOf(';')) > 0) {
                type = keystore.substring(i + 1);
                keystore = keystore.substring(0, i);
            } else type = KeyStore.getDefaultType();
            ks = KeyStore.getInstance(type);
            ks.load(new FileInputStream(keystore), secrets.getPassword("KEYSTORE"));
        }
        SSLContext context = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, secrets.getPassword("SSLCERT"));
        context.init(kmf.getKeyManagers(), null, null);
        return context.getServerSocketFactory();
    }
}
