package tests.database;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ElGamalKeyPairGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.ElGamalKeyGenerationParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.pfyshnet.bc_codec.PfyshLevel;
import org.pfyshnet.bc_codec.PfyshNodePrivateKeys;
import org.pfyshnet.bc_codec.PfyshNodePublicKeys;
import org.pfyshnet.core.DataStore;
import org.pfyshnet.core.DownloadSpecification;
import org.pfyshnet.core.GroupKey;
import org.pfyshnet.core.GroupKeyInfo;
import org.pfyshnet.core.Level;
import org.pfyshnet.core.LocalDataStore;
import org.pfyshnet.core.LocalSearchData;
import org.pfyshnet.core.LocalSearchSpecification;
import org.pfyshnet.core.MyNodeInfo;
import org.pfyshnet.core.NodeHello;
import org.pfyshnet.core.SearchData;
import org.pfyshnet.core.SearchSpecification;
import org.pfyshnet.utils.BytesToHex;
import org.pfyshnet.utils.FileKeeper;
import org.pfyshnet.utils.Utilities;

public class UtilitiesTester {

    public static int Certainty = 20;

    public static int ElGamalKeySize = 1024;

    public static int RSAKeySize = 768;

    public static int NumberKeys = 1000;

    public static int TestFileSize = 1024;

    private FileKeeper Keeper;

    private SecureRandom Random;

    public UtilitiesTester() {
        Random = new SecureRandom();
        Keeper = new FileKeeper("utilities_test", false, 0);
    }

    public AsymmetricCipherKeyPair genAsyncKeys() {
        BigInteger P = new BigInteger("ef9a661517dbd96296e34994a9e7ef86c527d776c33eaabfcd8df59837205c074111ec5a1fd1dcb7e54b2f370f84f57121cc9caadbeff4859eac1540ce4d2afe979d9e47c7be440cd43eb542f4e54ad312b7b8d78ecff8906251322d230f3ac16a732d070a4151006546fda4f73f319a52670cb01fcfb601680a2287ebcb2223", 16);
        BigInteger G = new BigInteger("fb649c87d572d0b4bf5052644cab31a75613acc7b4414704c391995e70b09b16a29dade2079d551c9300f3f1ed747448977853c4583f84ec795a305959741a7792be5ef721c6a2ee4b743e252b487d9bc9e9a3393da5fc7a48223fe3d8d86c7fafc543b9462e4024038ac5c6ae2f10793354e163318bff8e03db54dad0044", 16);
        ElGamalParameters parms = new ElGamalParameters(P, G);
        byte[] bt = new byte[16];
        for (int cnt = 0; cnt < 16; cnt++) {
            bt[cnt] = (byte) cnt;
        }
        byte[] tbytes = BytesToHex.hexToBytes("000102030405060708090A0B0C0D0E0F");
        if (!Arrays.equals(tbytes, bt)) {
            System.out.println("FAILED TO MATCH BYTES! " + BytesToHex.bytesToHex(tbytes));
        }
        ElGamalKeyGenerationParameters genparms = new ElGamalKeyGenerationParameters(Random, parms);
        ElGamalKeyPairGenerator keygen = new ElGamalKeyPairGenerator();
        keygen.init(genparms);
        return keygen.generateKeyPair();
    }

    public File getTestFile() throws IOException {
        File f = Keeper.getKeeperFile("dbtest");
        RandomAccessFile fos = new RandomAccessFile(f, "rw");
        for (int cnt = 0; cnt < 915; cnt++) {
            fos.writeInt(cnt);
        }
        fos.close();
        return f;
    }

    public void TestLocalDataStore() throws IOException {
        DataStore ds = new DataStore();
        ds.setData(getTestFile());
        ds.setLevel(new PfyshLevel(Random.nextLong(), 12));
        ds.setTag(Random.nextLong());
        LocalDataStore lds = new LocalDataStore();
        lds.setDataStore(ds);
        lds.setTimeStored(Random.nextLong());
        File f = Keeper.getKeeperFile("datastore");
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        Utilities.writeLocalDataStore(raf, lds);
        raf.close();
        raf = new RandomAccessFile(f, "rw");
        LocalDataStore lds2 = Utilities.readLocalDataStore(raf);
        raf.close();
        File f1 = (File) lds.getDataStore().getData();
        File f2 = (File) lds2.getDataStore().getData();
        if (!f1.equals(f2)) {
            throw new RuntimeException("1 Local DataStore failed!");
        }
        if (!lds.getDataStore().getLevel().equals(lds2.getDataStore().getLevel())) {
            throw new RuntimeException("2 Local DataStore failed!");
        }
        if (lds.getDataStore().getTag() != lds2.getDataStore().getTag()) {
            throw new RuntimeException("3 Local DataStore failed!");
        }
        if (lds.getTimeStored() != lds2.getTimeStored()) {
            throw new RuntimeException("4 Local DataStore failed!");
        }
        System.out.println("TestDataStore PASS.");
    }

    @SuppressWarnings("unchecked")
    public void TestDownloadSpec() throws IOException {
        LinkedList<ElGamalPrivateKeyParameters> GroupPrivateKeys = new LinkedList<ElGamalPrivateKeyParameters>();
        LinkedList<ElGamalPublicKeyParameters> GroupPublicKeys = new LinkedList<ElGamalPublicKeyParameters>();
        for (int cnt = 0; cnt < 10; cnt++) {
            AsymmetricCipherKeyPair keypair = genAsyncKeys();
            GroupPrivateKeys.add((ElGamalPrivateKeyParameters) keypair.getPrivate());
            GroupPublicKeys.add((ElGamalPublicKeyParameters) keypair.getPublic());
        }
        DownloadSpecification ds = new DownloadSpecification();
        ds.setGroupKeys(new LinkedList());
        for (int cnt2 = 0; cnt2 < 6; cnt2++) {
            ds.getGroupKeys().add(GroupPublicKeys.get(cnt2));
        }
        ds.setKey(Utilities.getSymmetricKey(Random));
        ds.setTag(30L);
        File f = Keeper.getKeeperFile("downspec");
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        Utilities.writeDownloadSpecification(raf, ds);
        raf.close();
        raf = new RandomAccessFile(f, "rw");
        DownloadSpecification ds2 = Utilities.readDownloadSpecification(raf);
        raf.close();
        ParametersWithIV ivparm = (ParametersWithIV) ds.getKey();
        KeyParameter keyparm = (KeyParameter) ivparm.getParameters();
        ParametersWithIV ivparm2 = (ParametersWithIV) ds2.getKey();
        KeyParameter keyparm2 = (KeyParameter) ivparm2.getParameters();
        if (!Arrays.equals(ivparm.getIV(), ivparm2.getIV())) {
            throw new RuntimeException("0 DownloadSpecification failed!");
        }
        if (!Arrays.equals(keyparm.getKey(), keyparm2.getKey())) {
            throw new RuntimeException("1 DownloadSpecification failed!");
        }
        if (ds.getTag() != ds2.getTag()) {
            throw new RuntimeException("2 DownloadSpecification failed!");
        }
        if (ds.getGroupKeys().size() != ds2.getGroupKeys().size()) {
            throw new RuntimeException("3 DownloadSpecification failed!");
        }
        Iterator i = ds.getGroupKeys().iterator();
        Iterator i2 = ds2.getGroupKeys().iterator();
        while (i.hasNext()) {
            ElGamalPublicKeyParameters e = (ElGamalPublicKeyParameters) i.next();
            ElGamalPublicKeyParameters e2 = (ElGamalPublicKeyParameters) i2.next();
            if (!e.getY().equals(e2.getY())) {
                throw new RuntimeException("4 DownloadSpecification failed!");
            }
            if (!e.getParameters().getG().equals(e2.getParameters().getG())) {
                throw new RuntimeException("5 DownloadSpecification failed!");
            }
            if (!e.getParameters().getP().equals(e2.getParameters().getP())) {
                throw new RuntimeException("6 DownloadSpecification failed!");
            }
            if (e.getParameters().getL() != e2.getParameters().getL()) {
                throw new RuntimeException("7 DownloadSpecification failed!");
            }
        }
        if (i2.hasNext()) {
            throw new RuntimeException("wow.. impressive");
        }
        System.out.println("TestDownloadSpec PASS.");
    }

    public void TestGroupKeyInfo() throws IOException {
        AsymmetricCipherKeyPair keypair = genAsyncKeys();
        AsymmetricCipherKeyPair keypair2 = genAsyncKeys();
        byte[] bt = new byte[20];
        byte[] bt2 = new byte[32];
        Random.nextBytes(bt);
        Random.nextBytes(bt2);
        RSAKeyGenerationParameters rsaparms = new RSAKeyGenerationParameters(BigInteger.valueOf(311L), Random, 32, 10);
        RSAKeyPairGenerator rsagen = new RSAKeyPairGenerator();
        rsagen.init(rsaparms);
        AsymmetricCipherKeyPair rsapair = rsagen.generateKeyPair();
        PfyshNodePublicKeys ppk = new PfyshNodePublicKeys();
        ppk.setEncryptionKey(keypair2.getPublic());
        ppk.setVerificationKey((RSAKeyParameters) rsapair.getPublic());
        NodeHello nh = new NodeHello();
        nh.setConnectionLocation("over here man!");
        nh.setHelloNumber(23L);
        nh.setPublicKey(ppk);
        nh.setSignature(bt2);
        GroupKey gk = new GroupKey();
        gk.setLevel(new PfyshLevel(Random.nextLong(), 10));
        gk.setPrivateKey(keypair.getPrivate());
        gk.setPublicKey(keypair.getPublic());
        gk.setSignature(bt);
        gk.setSourceNode(nh);
        GroupKeyInfo gki = new GroupKeyInfo(gk, Random.nextLong(), Random.nextLong());
        gki.setEncounters(10);
        gki.setReceivedTime(Random.nextLong());
        File f = Keeper.getKeeperFile("groupkey");
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        Utilities.writeGroupKeyInfo(raf, gki);
        raf.close();
        raf = new RandomAccessFile(f, "rw");
        GroupKeyInfo gki2 = Utilities.readGroupKeyInfo(raf);
        raf.close();
        if (gki.getEncounters() != gki2.getEncounters()) {
            throw new RuntimeException("0 GroupKeyInfo failed!");
        }
        if (gki.getReceivedTime() != gki2.getReceivedTime()) {
            throw new RuntimeException("1 GroupKeyInfo failed!");
        }
        if (!((Long) gki.getSourceNodeID()).equals((Long) gki2.getSourceNodeID())) {
            throw new RuntimeException("2 GroupKeyInfo failed!");
        }
        if (!gki.getGroupKey().getLevel().equals(gki2.getGroupKey().getLevel())) {
            throw new RuntimeException("3 GroupKeyInfo failed!");
        }
        byte[] b = (byte[]) gki.getGroupKey().getSignature();
        byte[] b2 = (byte[]) gki2.getGroupKey().getSignature();
        if (!Arrays.equals(b, b2)) {
            throw new RuntimeException("3.1 GroupKeyInfo failed!");
        }
        NodeHello n = gki.getGroupKey().getSourceNode();
        NodeHello n2 = gki2.getGroupKey().getSourceNode();
        if (!n.getConnectionLocation().equals(n2.getConnectionLocation())) {
            throw new RuntimeException("3.2 GroupKeyInfo failed!");
        }
        if (n.getHelloNumber() != n2.getHelloNumber()) {
            throw new RuntimeException("3.3 GroupKeyInfo failed!");
        }
        PfyshNodePublicKeys pnpk = (PfyshNodePublicKeys) n.getPublicKey();
        PfyshNodePublicKeys pnpk2 = (PfyshNodePublicKeys) n2.getPublicKey();
        RSAKeyParameters rkp = pnpk.getVerificationKey();
        RSAKeyParameters rkp2 = pnpk2.getVerificationKey();
        if (!rkp.getExponent().equals(rkp2.getExponent())) {
            throw new RuntimeException("3.3.1 GroupKeyInfo failed!");
        }
        if (!rkp.getModulus().equals(rkp2.getModulus())) {
            throw new RuntimeException("3.3.2 GroupKeyInfo failed!");
        }
        ElGamalPublicKeyParameters np = (ElGamalPublicKeyParameters) pnpk.getEncryptionKey();
        ElGamalPublicKeyParameters np2 = (ElGamalPublicKeyParameters) pnpk2.getEncryptionKey();
        if (!np.getY().equals(np2.getY())) {
            throw new RuntimeException("3.4 GroupKeyInfo failed!");
        }
        if (!np.getParameters().getG().equals(np2.getParameters().getG())) {
            throw new RuntimeException("3.5 GroupKeyInfo failed!");
        }
        if (!np.getParameters().getP().equals(np2.getParameters().getP())) {
            throw new RuntimeException("3.5 GroupKeyInfo failed!");
        }
        if (np.getParameters().getL() != np2.getParameters().getL()) {
            throw new RuntimeException("3.6 GroupKeyInfo failed!");
        }
        byte[] nb = (byte[]) n.getSignature();
        byte[] nb2 = (byte[]) n2.getSignature();
        if (!Arrays.equals(nb, nb2)) {
            throw new RuntimeException("3.7 GroupKeyInfo failed!");
        }
        ElGamalPrivateKeyParameters pr = (ElGamalPrivateKeyParameters) gki.getGroupKey().getPrivateKey();
        ElGamalPrivateKeyParameters pr2 = (ElGamalPrivateKeyParameters) gki2.getGroupKey().getPrivateKey();
        ElGamalPublicKeyParameters p = (ElGamalPublicKeyParameters) gki.getGroupKey().getPublicKey();
        ElGamalPublicKeyParameters p2 = (ElGamalPublicKeyParameters) gki2.getGroupKey().getPublicKey();
        if (!pr.getX().equals(pr2.getX())) {
            throw new RuntimeException("4 GroupKeyInfo failed!");
        }
        if (pr.getParameters().getL() != pr2.getParameters().getL()) {
            throw new RuntimeException("5 GroupKeyInfo failed!");
        }
        if (!pr.getParameters().getG().equals(pr2.getParameters().getG())) {
            throw new RuntimeException("6 GroupKeyInfo failed!");
        }
        if (!pr.getParameters().getP().equals(pr2.getParameters().getP())) {
            throw new RuntimeException("7 GroupKeyInfo failed!");
        }
        if (!p.getY().equals(p2.getY())) {
            throw new RuntimeException("8 GroupKeyInfo failed!");
        }
        if (!p.getParameters().getG().equals(p2.getParameters().getG())) {
            throw new RuntimeException("9 GroupKeyInfo failed!");
        }
        if (!p.getParameters().getP().equals(p2.getParameters().getP())) {
            throw new RuntimeException("10 GroupKeyInfo failed!");
        }
        if (p.getParameters().getL() != p2.getParameters().getL()) {
            throw new RuntimeException("11 GroupKeyInfo failed!");
        }
        System.out.println("TestGroupKeyInfo PASS.");
    }

    public void TestLocalSearchData() throws IOException {
        File td = getTestFile();
        SearchData sd = new SearchData();
        sd.setData(td);
        sd.setDepth(12);
        sd.setFullID(Random.nextLong());
        sd.setTag(Random.nextLong());
        LocalSearchData lsd = new LocalSearchData();
        lsd.setSearchData(sd);
        lsd.setStoreTime(Random.nextLong());
        File f = Keeper.getKeeperFile("groupkey");
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        Utilities.writeLocalSearchData(raf, lsd);
        raf.close();
        raf = new RandomAccessFile(f, "rw");
        LocalSearchData lsd2 = Utilities.readLocalSearchData(raf);
        raf.close();
        if (lsd.getStoreTime() != lsd2.getStoreTime()) {
            throw new RuntimeException("0 LocalSearchData failed!");
        }
        SearchData s = lsd.getSearchData();
        SearchData s2 = lsd2.getSearchData();
        if (s.getDepth() != s2.getDepth()) {
            throw new RuntimeException("1 LocalSearchData failed!");
        }
        if (!((Long) s.getFullID()).equals((Long) s2.getFullID())) {
            throw new RuntimeException("2 LocalSearchData failed!");
        }
        if (s.getTag() != s2.getTag()) {
            throw new RuntimeException("3 LocalSearchData failed!");
        }
        File fd = (File) s.getData();
        File fd2 = (File) s2.getData();
        if (!fd.equals(fd2)) {
            throw new RuntimeException("4 LocalSearchData failed!");
        }
        System.out.println("TestLocalSearchData PASS.");
    }

    @SuppressWarnings("unchecked")
    public void TestLocalSearchSpec() throws IOException {
        SearchSpecification ss = new SearchSpecification();
        ss.setGroupKeys(new LinkedList());
        for (int cnt = 0; cnt < 10; cnt++) {
            AsymmetricCipherKeyPair keypair = genAsyncKeys();
            ss.getGroupKeys().add(keypair.getPublic());
        }
        LocalSearchSpecification lss = new LocalSearchSpecification();
        lss.setSpec(ss);
        lss.setFullID((Long) Random.nextLong());
        lss.setTime(Random.nextLong());
        lss.setLevel(new PfyshLevel(Random.nextLong(), 10));
        File f = Keeper.getKeeperFile("searchspec");
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        Utilities.writeLocalSearchSpec(raf, lss);
        raf.close();
        raf = new RandomAccessFile(f, "rw");
        LocalSearchSpecification lss2 = Utilities.readLocalSearchSpec(raf);
        raf.close();
        if (lss.getTime() != lss2.getTime()) {
            throw new RuntimeException("0 LocalSearchSpecification failed!");
        }
        if (!lss.getFullID().equals(lss2.getFullID())) {
            throw new RuntimeException("1 LocalSearchSpecification failed!");
        }
        if (!lss.getLevel().equals(lss2.getLevel())) {
            throw new RuntimeException("2 LocalSearchSpecification failed!");
        }
        SearchSpecification s = lss.getSpec();
        SearchSpecification s2 = lss2.getSpec();
        if (s.getGroupKeys().size() != s2.getGroupKeys().size()) {
            throw new RuntimeException("3 LocalSearchSpecification failed!");
        }
        Iterator i = s.getGroupKeys().iterator();
        Iterator i2 = s2.getGroupKeys().iterator();
        while (i.hasNext()) {
            ElGamalPublicKeyParameters e = (ElGamalPublicKeyParameters) i.next();
            ElGamalPublicKeyParameters e2 = (ElGamalPublicKeyParameters) i2.next();
            if (!e.getY().equals(e2.getY())) {
                throw new RuntimeException("4 SearchSpecification failed!");
            }
            if (!e.getParameters().getG().equals(e2.getParameters().getG())) {
                throw new RuntimeException("5 SearchSpecification failed!");
            }
            if (!e.getParameters().getP().equals(e2.getParameters().getP())) {
                throw new RuntimeException("6 SearchSpecification failed!");
            }
            if (e.getParameters().getL() != e2.getParameters().getL()) {
                throw new RuntimeException("7 SearchSpecification failed!");
            }
        }
        if (i2.hasNext()) {
            throw new RuntimeException("wow.. impressive");
        }
        System.out.println("TestLocalSearchSpec PASS.");
    }

    public void TestMyNodeInfo() throws IOException {
        long fullid = Random.nextLong();
        Level[] l = new Level[31];
        for (int cnt = 0; cnt < 31; cnt++) {
            l[cnt] = new PfyshLevel(fullid, cnt);
        }
        AsymmetricCipherKeyPair keypair = genAsyncKeys();
        RSAKeyGenerationParameters rsaparms = new RSAKeyGenerationParameters(BigInteger.valueOf(311L), Random, 32, 10);
        RSAKeyPairGenerator rsagen = new RSAKeyPairGenerator();
        rsagen.init(rsaparms);
        AsymmetricCipherKeyPair rsapair = rsagen.generateKeyPair();
        PfyshNodePublicKeys ppk = new PfyshNodePublicKeys();
        ppk.setEncryptionKey(keypair.getPublic());
        ppk.setVerificationKey((RSAKeyParameters) rsapair.getPublic());
        PfyshNodePrivateKeys privk = new PfyshNodePrivateKeys();
        privk.setDecryptionKey((ElGamalPrivateKeyParameters) keypair.getPrivate());
        privk.setSignatureKey((RSAPrivateCrtKeyParameters) rsapair.getPrivate());
        byte[] bt = new byte[10];
        Random.nextBytes(bt);
        NodeHello nh = new NodeHello();
        nh.setConnectionLocation("over here man!");
        nh.setHelloNumber(23L);
        nh.setPublicKey(ppk);
        nh.setSignature(bt);
        MyNodeInfo my = new MyNodeInfo();
        my.setLevels(l);
        my.setPrivateKey(privk);
        my.setNode(nh);
        File f = Keeper.getKeeperFile("searchspec");
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        Utilities.writeMyNodeInfo(raf, my);
        raf.close();
        raf = new RandomAccessFile(f, "rw");
        MyNodeInfo my2 = Utilities.readMyNodeInfo(raf);
        raf.close();
        for (int cnt = 0; cnt < my.getLevels().length; cnt++) {
            if (!my.getLevels()[cnt].equals(my2.getLevels()[cnt])) {
                throw new RuntimeException("0 MyNodeInfo fail!");
            }
        }
        PfyshNodePrivateKeys myprivk = (PfyshNodePrivateKeys) my.getPrivateKey();
        PfyshNodePrivateKeys myprivk2 = (PfyshNodePrivateKeys) my2.getPrivateKey();
        ElGamalPrivateKeyParameters pr = (ElGamalPrivateKeyParameters) myprivk.getDecryptionKey();
        ElGamalPrivateKeyParameters pr2 = (ElGamalPrivateKeyParameters) myprivk2.getDecryptionKey();
        if (!pr.getX().equals(pr2.getX())) {
            throw new RuntimeException("4 GroupKeyInfo failed!");
        }
        if (pr.getParameters().getL() != pr2.getParameters().getL()) {
            throw new RuntimeException("5 GroupKeyInfo failed!");
        }
        if (!pr.getParameters().getG().equals(pr2.getParameters().getG())) {
            throw new RuntimeException("6 GroupKeyInfo failed!");
        }
        if (!pr.getParameters().getP().equals(pr2.getParameters().getP())) {
            throw new RuntimeException("7 GroupKeyInfo failed!");
        }
        RSAPrivateCrtKeyParameters rsa = myprivk.getSignatureKey();
        RSAPrivateCrtKeyParameters rsa2 = myprivk2.getSignatureKey();
        if (!rsa.getDP().equals(rsa2.getDP())) {
            throw new RuntimeException("blah");
        }
        if (!rsa.getDQ().equals(rsa2.getDQ())) {
            throw new RuntimeException("blah");
        }
        if (!rsa.getExponent().equals(rsa2.getExponent())) {
            throw new RuntimeException("blah");
        }
        if (!rsa.getModulus().equals(rsa2.getModulus())) {
            throw new RuntimeException("blah");
        }
        if (!rsa.getP().equals(rsa2.getP())) {
            throw new RuntimeException("blah");
        }
        if (!rsa.getPublicExponent().equals(rsa2.getPublicExponent())) {
            throw new RuntimeException("blah");
        }
        if (!rsa.getQ().equals(rsa2.getQ())) {
            throw new RuntimeException("blah");
        }
        if (!rsa.getQInv().equals(rsa2.getQInv())) {
            throw new RuntimeException("blah");
        }
        NodeHello n = my.getNode();
        NodeHello n2 = my2.getNode();
        if (!n.getConnectionLocation().equals(n2.getConnectionLocation())) {
            throw new RuntimeException("3.2 GroupKeyInfo failed!");
        }
        if (n.getHelloNumber() != n2.getHelloNumber()) {
            throw new RuntimeException("3.3 GroupKeyInfo failed!");
        }
        PfyshNodePublicKeys pnpk = (PfyshNodePublicKeys) n.getPublicKey();
        PfyshNodePublicKeys pnpk2 = (PfyshNodePublicKeys) n2.getPublicKey();
        RSAKeyParameters rkp = pnpk.getVerificationKey();
        RSAKeyParameters rkp2 = pnpk2.getVerificationKey();
        if (!rkp.getExponent().equals(rkp2.getExponent())) {
            throw new RuntimeException("3.3.1 GroupKeyInfo failed!");
        }
        if (!rkp.getModulus().equals(rkp2.getModulus())) {
            throw new RuntimeException("3.3.2 GroupKeyInfo failed!");
        }
        ElGamalPublicKeyParameters np = (ElGamalPublicKeyParameters) pnpk.getEncryptionKey();
        ElGamalPublicKeyParameters np2 = (ElGamalPublicKeyParameters) pnpk2.getEncryptionKey();
        if (!np.getY().equals(np2.getY())) {
            throw new RuntimeException("3.4 GroupKeyInfo failed!");
        }
        if (!np.getParameters().getG().equals(np2.getParameters().getG())) {
            throw new RuntimeException("3.5 GroupKeyInfo failed!");
        }
        if (!np.getParameters().getP().equals(np2.getParameters().getP())) {
            throw new RuntimeException("3.5 GroupKeyInfo failed!");
        }
        if (np.getParameters().getL() != np2.getParameters().getL()) {
            throw new RuntimeException("3.6 GroupKeyInfo failed!");
        }
        byte[] nb = (byte[]) n.getSignature();
        byte[] nb2 = (byte[]) n2.getSignature();
        if (!Arrays.equals(nb, nb2)) {
            throw new RuntimeException("3.7 GroupKeyInfo failed!");
        }
        System.out.println("TestMyNodeInfo PASS.");
    }

    public static void main(String args[]) {
        UtilitiesTester t = new UtilitiesTester();
        try {
            t.TestLocalDataStore();
            t.TestDownloadSpec();
            t.TestGroupKeyInfo();
            t.TestLocalSearchData();
            t.TestLocalSearchSpec();
            t.TestMyNodeInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
