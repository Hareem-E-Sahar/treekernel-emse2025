public class Test {    public void connect() throws Exception {
        String host = bean.getUrl();
        String s_port = bean.getPort();
        int port = Integer.parseInt(s_port);
        char[] passphrase = "changeit".toCharArray();
        File file = new File("jssecacerts");
        if (file.isFile() == false) {
            char SEP = File.separatorChar;
            File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
            file = new File(dir, "jssecacerts");
            if (file.isFile() == false) {
                file = new File(dir, "cacerts");
            }
        }
        System.out.println("Loading KeyStore " + file + "...");
        InputStream in = new FileInputStream(file);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
        in.close();
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[] { tm }, null);
        factory = context.getSocketFactory();
        System.out.println("Opening connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try {
            System.out.println("Starting SSL handshake...");
            socket.startHandshake();
            socket.close();
            System.out.println();
            System.out.println("No errors, certificate is already trusted");
        } catch (SSLException e) {
            System.out.println();
            e.printStackTrace(System.out);
        }
        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            System.out.println("Could not obtain server certificate chain");
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println();
        System.out.println("Server sent " + chain.length + " certificate(s):");
        System.out.println();
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
            System.out.println("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            System.out.println("   sha1    " + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
            System.out.println("   md5     " + toHexString(md5.digest()));
            System.out.println();
        }
        System.out.println("Enter certificate to add to trusted keystore or 'q' to quit: [1]");
        String line = reader.readLine().trim();
        int k;
        try {
            k = (line.length() == 0) ? 0 : Integer.parseInt(line) - 1;
        } catch (NumberFormatException e) {
            System.out.println("KeyStore not changed");
            return;
        }
        X509Certificate cert = chain[k];
        String alias = host + "-" + (k + 1);
        ks.setCertificateEntry(alias, cert);
        OutputStream out = new FileOutputStream("jssecacerts");
        ks.store(out, passphrase);
        out.close();
        System.out.println();
        System.out.println(cert);
        System.out.println();
        System.out.println("Added certificate to keystore 'jssecacerts' using alias '" + alias + "'");
    }
}