public class Test {    public void testCreateSocket() throws Exception {
        String password = "changeit";
        char[] pwd = password.toCharArray();
        RSAPrivateCrtKeySpec k;
        k = new RSAPrivateCrtKeySpec(new BigInteger(CertificatesToPlayWith.RSA_PUBLIC_MODULUS, 16), new BigInteger(CertificatesToPlayWith.RSA_PUBLIC_EXPONENT, 10), new BigInteger(CertificatesToPlayWith.RSA_PRIVATE_EXPONENT, 16), new BigInteger(CertificatesToPlayWith.RSA_PRIME1, 16), new BigInteger(CertificatesToPlayWith.RSA_PRIME2, 16), new BigInteger(CertificatesToPlayWith.RSA_EXPONENT1, 16), new BigInteger(CertificatesToPlayWith.RSA_EXPONENT2, 16), new BigInteger(CertificatesToPlayWith.RSA_COEFFICIENT, 16));
        PrivateKey pk = KeyFactory.getInstance("RSA").generatePrivate(k);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in1, in2, in3;
        in1 = new ByteArrayInputStream(CertificatesToPlayWith.X509_FOO);
        in2 = new ByteArrayInputStream(CertificatesToPlayWith.X509_INTERMEDIATE_CA);
        in3 = new ByteArrayInputStream(CertificatesToPlayWith.X509_ROOT_CA);
        X509Certificate[] chain = new X509Certificate[3];
        chain[0] = (X509Certificate) cf.generateCertificate(in1);
        chain[1] = (X509Certificate) cf.generateCertificate(in2);
        chain[2] = (X509Certificate) cf.generateCertificate(in3);
        ks.setKeyEntry("RSA_KEY", pk, pwd, chain);
        ks.setCertificateEntry("CERT", chain[2]);
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(ks, pwd);
        KeyManager[] keymanagers = kmfactory.getKeyManagers();
        TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(ks);
        TrustManager[] trustmanagers = tmfactory.getTrustManagers();
        SSLContext sslcontext = SSLContext.getInstance("TLSv1");
        sslcontext.init(keymanagers, trustmanagers, null);
        LocalTestServer server = new LocalTestServer(null, null, null, sslcontext);
        server.registerDefaultHandlers();
        server.start();
        try {
            TestX509HostnameVerifier hostnameVerifier = new TestX509HostnameVerifier();
            SSLSocketFactory socketFactory = new SSLSocketFactory(sslcontext);
            socketFactory.setHostnameVerifier(hostnameVerifier);
            Scheme https = new Scheme("https", socketFactory, 443);
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient.getConnectionManager().getSchemeRegistry().register(https);
            HttpHost target = new HttpHost(LocalTestServer.TEST_SERVER_ADDR.getHostName(), server.getServicePort(), "https");
            HttpGet httpget = new HttpGet("/random/100");
            HttpResponse response = httpclient.execute(target, httpget);
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertTrue(hostnameVerifier.isFired());
        } finally {
            server.stop();
        }
    }
}