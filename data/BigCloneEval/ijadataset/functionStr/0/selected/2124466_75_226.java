public class Test {    @SuppressWarnings("unchecked")
    public void Go(String testfilename) {
        File testfile = new File(testfilename);
        PfyshNodePublicKeys pub = new PfyshNodePublicKeys();
        PfyshNodePrivateKeys priv = new PfyshNodePrivateKeys();
        SecureRandom srand = new SecureRandom();
        ElGamalParametersGenerator generator = new ElGamalParametersGenerator();
        generator.init(768, 5, srand);
        ElGamalParameters ElGamalParms = generator.generateParameters();
        ElGamalKeyGenerationParameters genparms = new ElGamalKeyGenerationParameters(srand, ElGamalParms);
        ElGamalKeyPairGenerator keygen = new ElGamalKeyPairGenerator();
        keygen.init(genparms);
        AsymmetricCipherKeyPair keypair = keygen.generateKeyPair();
        pub.setEncryptionKey((ElGamalPublicKeyParameters) keypair.getPublic());
        priv.setDecryptionKey((ElGamalPrivateKeyParameters) keypair.getPrivate());
        RSAKeyGenerationParameters rsaparms = new RSAKeyGenerationParameters(BigInteger.valueOf(1001), srand, 768, 10);
        RSAKeyPairGenerator rsagen = new RSAKeyPairGenerator();
        rsagen.init(rsaparms);
        AsymmetricCipherKeyPair rsapair = rsagen.generateKeyPair();
        pub.setVerificationKey((RSAKeyParameters) rsapair.getPublic());
        priv.setSignatureKey((RSAPrivateCrtKeyParameters) rsapair.getPrivate());
        MyData = new MyNodeInfo();
        NodeHello nh = new NodeHello();
        nh.setPublicKey(pub);
        nh.setSignature(new byte[20]);
        nh.setConnectionLocation("127.0.0.1;30020");
        MyData.setPrivateKey(priv);
        MyData.setLevels(CalculateLevels(pub));
        MyData.setNode(nh);
        ComSet = new PfyshComSettings();
        ComSet.setPort(30020);
        ComSet.setRootDir("comroot");
        ComSet.setMaxIncoming(5);
        ComSet.setMaxOutgoing(5);
        Com = new PfyshCom(this);
        Com.setMyNodeInfo(MyData);
        LinkedList<DataTransfer> xfers = new LinkedList<DataTransfer>();
        DataStore datastore = new DataStore();
        datastore.setData(testfile);
        datastore.setLevel(new PfyshLevel(10L, 10));
        datastore.setTag(10);
        TestTransfer tt = new TestTransfer(nh, datastore);
        xfers.add(tt);
        Com.PushStore(xfers);
        xfers.clear();
        GroupQuery gq = new GroupQuery();
        gq.setBackTime(10L);
        gq.setLevel(new PfyshLevel(10L, 10));
        gq.setNode(nh);
        tt = new TestTransfer(nh, gq);
        xfers.add(tt);
        Com.QueryForData(xfers);
        xfers.clear();
        gq = new GroupQuery();
        gq.setBackTime(10L);
        gq.setLevel(new PfyshLevel(10L, 10));
        gq.setNode(nh);
        tt = new TestTransfer(nh, gq);
        xfers.add(tt);
        Com.QueryForKeys(xfers);
        xfers.clear();
        tt = new TestTransfer(nh, nh);
        xfers.add(tt);
        Com.QueryForNodes(xfers);
        xfers.clear();
        tt = new TestTransfer(nh, nh);
        xfers.add(tt);
        Com.QueryForSearchSpecs(xfers);
        xfers.clear();
        SearchSpecification ss = new SearchSpecification();
        LinkedList<ElGamalPublicKeyParameters> keys = new LinkedList<ElGamalPublicKeyParameters>();
        keys.add((ElGamalPublicKeyParameters) pub.getEncryptionKey());
        keys.add((ElGamalPublicKeyParameters) pub.getEncryptionKey());
        keys.add((ElGamalPublicKeyParameters) pub.getEncryptionKey());
        ss.setGroupKeys((List) keys);
        tt = new TestTransfer(nh, ss);
        xfers.add(tt);
        Com.SearchSpecification(xfers);
        xfers.clear();
        LinkedList<NodeHello> nl = new LinkedList<NodeHello>();
        nl.add(nh);
        nl.add(nh);
        tt = new TestTransfer(nh, nl);
        xfers.add(tt);
        Com.SendHellos(xfers);
        xfers.clear();
        LinkedList<GroupKey> gkl = new LinkedList<GroupKey>();
        GroupKey gk = new GroupKey();
        gk.setLevel(new PfyshLevel(10L, 10));
        gk.setPrivateKey(priv.getDecryptionKey());
        gk.setPublicKey(pub.getEncryptionKey());
        gk.setSignature(new byte[10]);
        gk.setSourceNode(nh);
        gkl.add(gk);
        gkl.add(gk);
        tt = new TestTransfer(nh, gkl);
        xfers.add(tt);
        Com.SendNewKeys(xfers);
        LinkedList<RouteTransfer> rxfers = new LinkedList<RouteTransfer>();
        TestRouteTransfer tr = new TestRouteTransfer();
        tr.setDepth(5);
        tr.setDestination(nh);
        tr.setHops(3);
        tr.setPayload(testfile);
        rxfers.add(tr);
        Com.SendRoutes(rxfers);
        xfers.clear();
        SearchData sd = new SearchData();
        sd.setData(testfile);
        sd.setDepth(5);
        sd.setFullID(25L);
        sd.setTag(13L);
        tt = new TestTransfer(nh, sd);
        xfers.add(tt);
        Com.SendSearchData(xfers);
        xfers.clear();
        LinkedList<SearchSpecification> speclist = new LinkedList<SearchSpecification>();
        speclist.add(ss);
        speclist.add(ss);
        tt = new TestTransfer(nh, speclist);
        xfers.add(tt);
        Com.SendSearchSpecQuery(xfers);
        xfers.clear();
        StoreRequest sr = new StoreRequest();
        sr.setData(testfile);
        sr.setEncodedReturn(testfile);
        KeyParameter kp = new KeyParameter(new byte[10]);
        ParametersWithIV piv = new ParametersWithIV(kp, new byte[10]);
        sr.setReturnKey(piv);
        tt = new TestTransfer(nh, sr);
        xfers.add(tt);
        Com.SendStores(xfers);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean allpass = true;
        for (int cnt = 0; cnt < 11; cnt++) {
            if (Hit[cnt]) {
                System.out.println("PASS: " + cnt);
            } else {
                System.out.println("FAIL: " + cnt);
                allpass = false;
            }
        }
        if (allpass) {
            System.out.println("ALL PASSED!");
        } else {
            System.out.println("THERE WAS A FAILURE.");
        }
    }
}