package tests.codec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.generators.ElGamalKeyPairGenerator;
import org.bouncycastle.crypto.params.ElGamalKeyGenerationParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.pfyshnet.bc_codec.PfyshCodec;
import org.pfyshnet.bc_codec.PfyshCodecSettings;
import org.pfyshnet.bc_codec.PfyshLevel;
import org.pfyshnet.bc_codec.PfyshNodePrivateKeys;
import org.pfyshnet.bc_codec.PfyshNodePublicKeys;
import org.pfyshnet.core.CodecInterface;
import org.pfyshnet.core.CoreCodecInterface;
import org.pfyshnet.core.DecodedDDRData;
import org.pfyshnet.core.DownloadSpecification;
import org.pfyshnet.core.EncodedTransfer;
import org.pfyshnet.core.EncodedTransferFromNode;
import org.pfyshnet.core.GroupKey;
import org.pfyshnet.core.Level;
import org.pfyshnet.core.LocalSearchData;
import org.pfyshnet.core.LocalSearchSpecification;
import org.pfyshnet.core.MyNodeInfo;
import org.pfyshnet.core.NodeHello;
import org.pfyshnet.core.NodeInfo;
import org.pfyshnet.core.RetrieveRequest;
import org.pfyshnet.core.ReturnData;
import org.pfyshnet.core.RouteTransfer;
import org.pfyshnet.core.SearchData;
import org.pfyshnet.core.SearchRequest;
import org.pfyshnet.core.SearchSpecification;
import org.pfyshnet.core.StoreRequest;
import org.pfyshnet.utils.BCUtils;
import org.pfyshnet.utils.BytesToHex;
import org.pfyshnet.utils.DiffFiles;

public class CodecTester implements CoreCodecInterface {

    public static int Certainty = 20;

    public static int ElGamalKeySize = 1024;

    public static int RSAKeySize = 768;

    public static int NumberKeys = 1000;

    public static int TestFileSize = 1024;

    private RetrieveRequest DataQuery;

    private EncodedTransferFromNode DecodeFailed;

    private DecodedDDRData DecodedDDRRoute;

    private ReturnData DownloadReturn;

    private EncodedTransferFromNode NewData;

    private GroupKey NewGroupKey;

    private SearchRequest SearchQuery;

    private ReturnData SearchReturn;

    private SearchSpecification SearchSpecification;

    private RouteTransfer SendRoute;

    private StoreRequest StoreRequest;

    private SearchData StoreSearch;

    private ReturnData UploadReturn;

    private SecureRandom Rand;

    private LinkedList<ElGamalPublicKeyParameters> GroupPublicKeys;

    private LinkedList<ElGamalPrivateKeyParameters> GroupPrivateKeys;

    private CodecInterface Codec;

    private MyNodeInfo MyNode;

    private void ResetAll() {
        DataQuery = null;
        DecodeFailed = null;
        DecodedDDRRoute = null;
        DownloadReturn = null;
        NewData = null;
        SearchQuery = null;
        SearchReturn = null;
        SearchSpecification = null;
        SendRoute = null;
        StoreRequest = null;
        StoreSearch = null;
        UploadReturn = null;
    }

    private void CheckSet() {
        if (DataQuery != null) {
            System.out.println(">>> DataQuery");
        }
        if (DecodeFailed != null) {
            System.out.println(">>> DecodeFailed");
        }
        if (DecodedDDRRoute != null) {
            System.out.println(">>> DecodedDDRRoute");
        }
        if (DownloadReturn != null) {
            System.out.println(">>> DownloadReturn");
        }
        if (NewData != null) {
            System.out.println(">>> NewData");
        }
        if (NewGroupKey != null) {
            System.out.println(">>> NewGroupKey");
        }
        if (SearchQuery != null) {
            System.out.println(">>> SearchQuery");
        }
        if (SearchReturn != null) {
            System.out.println(">>> SearchReturn");
        }
        if (SearchSpecification != null) {
            System.out.println(">>> SearchSpecification");
        }
        if (SendRoute != null) {
            System.out.println(">>> SendRoute");
        }
        if (StoreRequest != null) {
            System.out.println(">>> StoreRequest");
        }
        if (StoreSearch != null) {
            System.out.println(">>> StoreSearch");
        }
        if (UploadReturn != null) {
            System.out.println(">>> UploadReturn");
        }
    }

    @SuppressWarnings("unchecked")
    public void run() {
        BigInteger P = new BigInteger("ef9a661517dbd96296e34994a9e7ef86c527d776c33eaabfcd8df59837205c074111ec5a1fd1dcb7e54b2f370f84f57121cc9caadbeff4859eac1540ce4d2afe979d9e47c7be440cd43eb542f4e54ad312b7b8d78ecff8906251322d230f3ac16a732d070a4151006546fda4f73f319a52670cb01fcfb601680a2287ebcb2223", 16);
        BigInteger G = new BigInteger("fb649c87d572d0b4bf5052644cab31a75613acc7b4414704c391995e70b09b16a29dade2079d551c9300f3f1ed747448977853c4583f84ec795a305959741a7792be5ef721c6a2ee4b743e252b487d9bc9e9a3393da5fc7a48223fe3d8d86c7fafc543b9462e4024038ac5c6ae2f10793354e163318bff8e03db54dad0044", 16);
        ElGamalParameters parms = new ElGamalParameters(P, G);
        byte[] bt = new byte[16];
        for (int cnt = 0; cnt < 16; cnt++) {
            bt[cnt] = (byte) cnt;
        }
        System.out.println("S: " + BytesToHex.bytesToHex(bt));
        byte[] tbytes = BytesToHex.hexToBytes("000102030405060708090A0B0C0D0E0F");
        if (!Arrays.equals(tbytes, bt)) {
            System.out.println("FAILED TO MATCH BYTES! " + BytesToHex.bytesToHex(tbytes));
        }
        Rand = new SecureRandom();
        PfyshCodecSettings set = new PfyshCodecSettings();
        set.setCertainty(Certainty);
        set.setFileKeeperDir("codecdir");
        set.setDeleteWipePasses(2);
        set.setPrivateKeySize(ElGamalKeySize);
        set.setRSACertainty(Certainty);
        set.setRSAPublicExponent(BigInteger.valueOf(311));
        set.setRSASize(RSAKeySize);
        PfyshCodec codec = new PfyshCodec(parms, set);
        Codec = codec;
        MyNode = new MyNodeInfo();
        NodeHello myhello = new NodeHello();
        myhello.setConnectionLocation("127.0.0.1");
        myhello.setHelloNumber(0);
        myhello.setSignature(new byte[32]);
        MyNode.setNode(myhello);
        Codec.setMyNodeInfo(MyNode);
        Codec.SignMyNodeInfo(MyNode);
        PfyshNodePublicKeys pub = (PfyshNodePublicKeys) MyNode.getNode().getPublicKey();
        System.out.println("Codec keys generated!");
        Codec.GenerateGroupKeys(MyNode, MyNode.getLevels()[4], this);
        byte[] testbytes = new byte[72];
        for (int cnt = 0; cnt < testbytes.length; cnt++) {
            testbytes[cnt] = (byte) cnt;
        }
        PfyshNodePrivateKeys priv = (PfyshNodePrivateKeys) MyNode.getPrivateKey();
        try {
            byte[] encbytes = BCUtils.EncryptHeader(testbytes, BCUtils.genK((ElGamalPublicKeyParameters) pub.getEncryptionKey(), Rand));
            System.out.println(">>> " + BytesToHex.bytesToHex(encbytes));
            byte[] decbytes = BCUtils.DecryptHeader(encbytes, priv.getDecryptionKey());
            if (!Arrays.equals(testbytes, decbytes)) {
                System.out.println("Header encrypt decrypt failed!");
                System.exit(0);
            }
        } catch (InvalidCipherTextException e1) {
            e1.printStackTrace();
        }
        GroupPrivateKeys = new LinkedList<ElGamalPrivateKeyParameters>();
        GroupPublicKeys = new LinkedList<ElGamalPublicKeyParameters>();
        System.out.println("ElGamal Parameters generated!");
        for (int cnt = 0; cnt < NumberKeys; cnt++) {
            ElGamalKeyGenerationParameters genparms = new ElGamalKeyGenerationParameters(Rand, ((ElGamalPublicKeyParameters) pub.getEncryptionKey()).getParameters());
            ElGamalKeyPairGenerator keygen = new ElGamalKeyPairGenerator();
            keygen.init(genparms);
            AsymmetricCipherKeyPair keypair = keygen.generateKeyPair();
            GroupPrivateKeys.add((ElGamalPrivateKeyParameters) keypair.getPrivate());
            GroupPublicKeys.add((ElGamalPublicKeyParameters) keypair.getPublic());
        }
        try {
            boolean pass = true;
            byte[] encbytes = BCUtils.EncryptHeader(testbytes, BCUtils.genK(GroupPublicKeys.get(0), Rand));
            byte[] decbytes = BCUtils.DecryptHeader(encbytes, GroupPrivateKeys.get(0));
            if (!Arrays.equals(decbytes, testbytes)) {
                System.out.println("Failed to decoded header!");
                System.exit(0);
            }
            File f = getTestFile();
            File encfile = File.createTempFile("blah", ".enc");
            Object key = BCUtils.getSymmetricKey(Rand);
            BCUtils.EncryptFile(f, encfile, key, false);
            File decfile = File.createTempFile("blah2", ".dec");
            BCUtils.DecryptFile(encfile, decfile, key, false);
            if (DiffFiles.diffFiles(decfile, f)) {
                System.out.println("Simple file encrypt decrypt passed!");
            } else {
                System.out.println("Simple file encrypt decrypt FAILED!");
                System.exit(0);
            }
            NodeHello hello = new NodeHello();
            NodeInfo info = new NodeInfo();
            info.setHello(hello);
            EncodedTransfer encxfer = new EncodedTransfer();
            encxfer.setHops(3);
            encxfer.setLayerDepth(2);
            encxfer.setPayload(null);
            EncodedTransferFromNode xfer = new EncodedTransferFromNode();
            xfer.setSourceNode(hello);
            xfer.setSourceNodeInfo(info);
            xfer.setTransfer(encxfer);
            xfer.getTransfer().setPayload(f);
            Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
            if (DecodeFailed != xfer) {
                pass = false;
                System.out.println("Bad decode test failed!!");
            }
            ResetAll();
            pass = DownloadTest();
            if (!pass) {
                System.out.println("DOWNLOAD FAILED!");
                System.exit(0);
            }
            ResetAll();
            pass = UploadTest();
            if (!pass) {
                System.out.println("UPLOAD FAILED!");
                System.exit(0);
            }
            ResetAll();
            pass = SearchRequestTest();
            if (!pass) {
                System.out.println("SEARCH FAILED!");
                System.exit(0);
            }
            ResetAll();
            pass = SearchStoreTest();
            if (!pass) {
                System.out.println("SEARCH STORE FAILED!");
                System.exit(0);
            }
            pass = SearchSpecTest();
            if (!pass) {
                System.out.println("SEARCH SPEC FAILED!");
                System.exit(0);
            }
            pass = TestSearchSpecHash();
            if (!pass) {
                System.out.println("SEARCH SPEC HASH FAILED!");
                System.exit(0);
            }
            pass = TestLevels();
            if (!pass) {
                System.out.println("LEVEL CHECK FAILED!");
                System.exit(0);
            }
            long id = (Long) Codec.GenerateFullID("Test search string");
            System.out.println("Search string fullid: " + id);
            pass = TestSignatures();
            if (!pass) {
                System.out.println("SIGNATURES FAILED!");
                System.exit(0);
            }
            PfyshLevel l0 = (PfyshLevel) Codec.getLevel(0x0123456789abcdefL, 4);
            PfyshLevel l1 = (PfyshLevel) Codec.getLevel(0x0113456789abcdefL, 4);
            System.out.println("L0: " + Long.toHexString((Long) l0.getID()));
            System.out.println("L1: " + Long.toHexString((Long) l1.getID()));
            List<Level> ls = Codec.getOtherLevels(l0, l1);
            if (ls.size() != 2) {
                System.out.println("1) Other levels failed! " + ls.size());
                for (int cnt = 0; cnt < ls.size(); cnt++) {
                    System.out.println(">> " + Long.toHexString((Long) ls.get(cnt).getID()));
                }
                System.exit(0);
            }
            if (ls.get(0).equals(ls.get(1))) {
                System.out.println("3) Other levels failed!");
                System.exit(0);
            }
            for (int cnt = 0; cnt < 2; cnt++) {
                PfyshLevel lt = (PfyshLevel) ls.get(cnt);
                if (!(lt.getID().equals(0x0130000000000000L) || lt.getID().equals(0x0100000000000000L))) {
                    System.out.println("2) Other levels failed!");
                    System.exit(0);
                }
            }
            if (pass) {
                System.out.println("Other levels passed!");
            }
            pass = TestGroupKeys();
            if (!pass) {
                System.out.println("TEST GROUP KEYS FAILED!");
                System.exit(0);
            }
            if (pass) {
                System.out.println(" --  ALL PASSED  --");
            } else {
                System.out.println(" -- TEST FAILED --");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public File getTestFile() throws IOException {
        byte[] buffer = new byte[1024];
        File f = File.createTempFile("blah", ".dat");
        FileOutputStream fos = new FileOutputStream(f);
        for (int cnt = 0; cnt < buffer.length; cnt++) {
            Rand.nextBytes(buffer);
            fos.write(buffer);
        }
        fos.close();
        return f;
    }

    @SuppressWarnings("unchecked")
    public boolean DownloadTest() throws Exception {
        boolean pass = true;
        File f = getTestFile();
        NodeHello hello = new NodeHello();
        NodeInfo info = new NodeInfo();
        info.setHello(hello);
        EncodedTransfer encxfer = new EncodedTransfer();
        encxfer.setHops(3);
        encxfer.setLayerDepth(2);
        encxfer.setPayload(null);
        EncodedTransferFromNode xfer = new EncodedTransferFromNode();
        xfer.setSourceNode(hello);
        xfer.setSourceNodeInfo(info);
        xfer.setTransfer(encxfer);
        File dri = (File) Codec.getDownloadReturnInstr(20, null);
        NodeHello[] hl = new NodeHello[2];
        hl[0] = Codec.CloneNode(MyNode.getNode());
        hl[1] = Codec.CloneNode(MyNode.getNode());
        Object symkey1 = Codec.getNewSymmetricKey();
        File enc1file = (File) Codec.EncodeNodeRoute(dri, symkey1, hl);
        Object symkey2 = Codec.getNewSymmetricKey();
        File origonion = (File) Codec.EncodeNodeRoute(enc1file, symkey2, hl);
        Object returnkey = Codec.getNewSymmetricKey();
        RetrieveRequest rr = new RetrieveRequest();
        rr.setDataEncodeKey(returnkey);
        rr.setEncodedReturn(origonion);
        rr.setTag(20L);
        File onionfile = (File) Codec.BundleDownload(rr);
        for (int cnt = 0; cnt < 10; cnt++) {
            int idx = (int) ((double) GroupPublicKeys.size() * Rand.nextDouble());
            Object enckey = GroupPublicKeys.get(idx);
            onionfile = (File) Codec.EncodeDDR(onionfile, Codec.getNewSymmetricKey(), enckey);
        }
        long StartTime = (new Date()).getTime();
        for (int cnt = 0; cnt < 10; cnt++) {
            xfer.getTransfer().setPayload(onionfile);
            Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
            if (DecodedDDRRoute != null) {
                onionfile = (File) DecodedDDRRoute.getDecodedData();
                ResetAll();
            } else if (DataQuery != null) {
                if (DataQuery.getTag() == 20L && cnt == 9) {
                    System.out.println("DDR Decode Passed!");
                    Object onion = Codec.BundleDataReturn(f, DataQuery.getDataEncodeKey(), DataQuery.getEncodedReturn());
                    ResetAll();
                    onion = Codec.EncodeNodeRoute(onion, Codec.getNewSymmetricKey(), hl);
                    xfer.getTransfer().setPayload(onion);
                    Codec.ProcessNodeTransfer(onion, this);
                    if (SendRoute != null) {
                        xfer.getTransfer().setPayload(SendRoute.getPayload());
                        ResetAll();
                        Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                        if (SendRoute != null) {
                            RouteTransfer sr = SendRoute;
                            ResetAll();
                            sr.Failure();
                            if (SendRoute != null) {
                                xfer.getTransfer().setPayload(SendRoute.getPayload());
                                ResetAll();
                                Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                                if (SendRoute != null) {
                                    xfer.getTransfer().setPayload(SendRoute.getPayload());
                                    ResetAll();
                                    Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                                    if (DownloadReturn != null) {
                                        if (DownloadReturn.getID() == 20) {
                                            File dr = (File) DownloadReturn.getData();
                                            dri = (File) Codec.getDownloadReturnInstr(20, dr);
                                            enc1file = (File) Codec.EncodeNodeRoute(dri, symkey1, hl);
                                            enc1file = (File) Codec.EncodeNodeRoute(enc1file, symkey2, hl);
                                            File rfile = (File) Codec.ExtractAttachedData(origonion, enc1file, returnkey);
                                            if (DiffFiles.subDiffFiles(rfile, f)) {
                                                System.out.println("DOWNLOAD RETURN TEST PASSED!");
                                            } else {
                                                System.out.println("7) Download file missmatch! " + f.getPath() + " <> " + rfile.getPath());
                                                CheckSet();
                                                pass = false;
                                            }
                                        } else {
                                            System.out.println("6) Return ID did not match!");
                                            CheckSet();
                                            pass = false;
                                        }
                                    } else {
                                        System.out.println("5) Download return failed!");
                                        CheckSet();
                                        pass = false;
                                    }
                                } else {
                                    System.out.println("4) Failed download return!");
                                    CheckSet();
                                    pass = false;
                                }
                            } else {
                                System.out.println("3) Failed failed route transfer on download return!");
                                CheckSet();
                                pass = false;
                            }
                        } else {
                            System.out.println("2) Failed download return!");
                            CheckSet();
                            pass = false;
                        }
                    } else {
                        System.out.println("1) Failed download return!");
                        CheckSet();
                        pass = false;
                    }
                    ResetAll();
                } else {
                    System.out.println("DDR Decode failed! tag: " + DataQuery.getTag() + " cnt: " + cnt);
                    pass = false;
                }
            } else {
                System.out.println("DDR Decode failed!");
                pass = false;
                CheckSet();
            }
            ResetAll();
        }
        long EndTime = (new Date()).getTime();
        long SecondsElapsed = (EndTime - StartTime) / 1000;
        System.out.println("Seconds for decode: " + SecondsElapsed);
        return pass;
    }

    @SuppressWarnings("unchecked")
    public boolean UploadTest() throws Exception {
        boolean pass = true;
        File f = getTestFile();
        NodeHello hello = new NodeHello();
        NodeInfo info = new NodeInfo();
        info.setHello(hello);
        EncodedTransfer encxfer = new EncodedTransfer();
        encxfer.setHops(3);
        encxfer.setLayerDepth(2);
        encxfer.setPayload(null);
        EncodedTransferFromNode xfer = new EncodedTransferFromNode();
        xfer.setSourceNode(hello);
        xfer.setSourceNodeInfo(info);
        xfer.setTransfer(encxfer);
        File dri = (File) Codec.getUploadReturnInstr(20, null);
        NodeHello[] hl = new NodeHello[2];
        hl[0] = Codec.CloneNode(MyNode.getNode());
        hl[1] = Codec.CloneNode(MyNode.getNode());
        Object symkey1 = Codec.getNewSymmetricKey();
        File enc1file = (File) Codec.EncodeNodeRoute(dri, symkey1, hl);
        Object symkey2 = Codec.getNewSymmetricKey();
        File origonion = (File) Codec.EncodeNodeRoute(enc1file, symkey2, hl);
        Object returnkey = Codec.getNewSymmetricKey();
        StoreRequest sreq = new StoreRequest();
        sreq.setData(f);
        sreq.setEncodedReturn(origonion);
        sreq.setReturnKey(returnkey);
        File onionfile = (File) Codec.BundleUpload(sreq);
        enc1file = (File) Codec.EncodeNodeRoute(onionfile, Codec.getNewSymmetricKey(), hl);
        enc1file = (File) Codec.EncodeNodeRoute(enc1file, Codec.getNewSymmetricKey(), hl);
        enc1file = (File) Codec.EncodeNodeRoute(enc1file, Codec.getNewSymmetricKey(), hl);
        Codec.ProcessNodeTransfer(enc1file, this);
        if (SendRoute != null) {
            onionfile = (File) SendRoute.getPayload();
            for (int cnt = 0; cnt < 3; cnt++) {
                xfer.getTransfer().setPayload(onionfile);
                Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                if (SendRoute != null) {
                    onionfile = (File) SendRoute.getPayload();
                    ResetAll();
                } else if (StoreRequest != null) {
                    DownloadSpecification ds = new DownloadSpecification();
                    ds.setGroupKeys(new LinkedList());
                    for (int cnt2 = 0; cnt2 < 6; cnt2++) {
                        ds.getGroupKeys().add(GroupPublicKeys.get(cnt2));
                    }
                    Object dskey = Codec.getNewSymmetricKey();
                    Codec.EncodeStore(StoreRequest.getData(), dskey);
                    ds.setKey(dskey);
                    ds.setTag(30L);
                    Object onion = Codec.BundleDownloadSpecification(ds, StoreRequest.getReturnKey(), StoreRequest.getEncodedReturn());
                    ResetAll();
                    onion = Codec.EncodeNodeRoute(onion, Codec.getNewSymmetricKey(), hl);
                    xfer.getTransfer().setPayload(onion);
                    Codec.ProcessNodeTransfer(onion, this);
                    if (SendRoute != null) {
                        xfer.getTransfer().setPayload(SendRoute.getPayload());
                        ResetAll();
                        Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                        if (SendRoute != null) {
                            RouteTransfer sr = SendRoute;
                            ResetAll();
                            sr.Failure();
                            if (SendRoute != null) {
                                xfer.getTransfer().setPayload(SendRoute.getPayload());
                                ResetAll();
                                Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                                if (SendRoute != null) {
                                    xfer.getTransfer().setPayload(SendRoute.getPayload());
                                    ResetAll();
                                    Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                                    if (UploadReturn != null) {
                                        if (UploadReturn.getID() == 20) {
                                            File dr = (File) UploadReturn.getData();
                                            dri = (File) Codec.getUploadReturnInstr(20, dr);
                                            enc1file = (File) Codec.EncodeNodeRoute(dri, symkey1, hl);
                                            enc1file = (File) Codec.EncodeNodeRoute(enc1file, symkey2, hl);
                                            File rfile = (File) Codec.ExtractAttachedData(origonion, enc1file, returnkey);
                                            DownloadSpecification dsr = Codec.ExtractDownloadSpec(rfile);
                                            if (dsr.getTag() == ds.getTag()) {
                                                if (dsr.getGroupKeys().size() == ds.getGroupKeys().size()) {
                                                    for (int cnt2 = 0; cnt2 < ds.getGroupKeys().size() && pass; cnt2++) {
                                                        ElGamalPublicKeyParameters dspub = (ElGamalPublicKeyParameters) ds.getGroupKeys().get(cnt2);
                                                        ElGamalPublicKeyParameters dsrpub = (ElGamalPublicKeyParameters) dsr.getGroupKeys().get(cnt2);
                                                        if (dspub.getParameters().getP().equals(dsrpub.getParameters().getP()) && dspub.getParameters().getG().equals(dsrpub.getParameters().getG()) && dspub.getY().equals(dsrpub.getY())) {
                                                            System.out.println("Key OK!");
                                                        } else {
                                                            System.out.println("9) Return key mismatch!");
                                                            pass = false;
                                                        }
                                                    }
                                                    if (pass) {
                                                        ParametersWithIV dsrparm = (ParametersWithIV) dsr.getKey();
                                                        ParametersWithIV dsparm = (ParametersWithIV) ds.getKey();
                                                        pass = Arrays.equals(dsrparm.getIV(), dsparm.getIV());
                                                        KeyParameter dsk = (KeyParameter) dsparm.getParameters();
                                                        KeyParameter dsrk = (KeyParameter) dsrparm.getParameters();
                                                        pass = pass && Arrays.equals(dsk.getKey(), dsrk.getKey());
                                                        if (!pass) {
                                                            System.out.println("10) Return data key mismatch!");
                                                        }
                                                    }
                                                } else {
                                                    System.out.println("8) Incorrect number of DS keys!");
                                                    pass = false;
                                                }
                                            } else {
                                                System.out.println("7) Incorrect DS tag!");
                                                pass = false;
                                            }
                                        } else {
                                            System.out.println("6) Return ID did not match!");
                                            CheckSet();
                                            pass = false;
                                        }
                                    } else {
                                        System.out.println("5) Upload return failed!");
                                        CheckSet();
                                        pass = false;
                                    }
                                } else {
                                    System.out.println("4) Failed Upload return!");
                                    CheckSet();
                                    pass = false;
                                }
                            } else {
                                System.out.println("3) Failed failed route transfer on upload return!");
                                CheckSet();
                                pass = false;
                            }
                        } else {
                            System.out.println("2) Failed upload return!");
                            CheckSet();
                            pass = false;
                        }
                    } else {
                        System.out.println("1) Failed upload return!");
                        CheckSet();
                        pass = false;
                    }
                    ResetAll();
                } else {
                    System.out.println("Invalid instruction!");
                    pass = false;
                    CheckSet();
                }
                ResetAll();
            }
        } else {
            System.out.println("Failed to send onion!");
            pass = false;
        }
        return pass;
    }

    @SuppressWarnings("unchecked")
    public boolean SearchRequestTest() throws IOException {
        boolean pass = true;
        File f = getTestFile();
        NodeHello hello = new NodeHello();
        NodeInfo info = new NodeInfo();
        info.setHello(hello);
        EncodedTransfer encxfer = new EncodedTransfer();
        encxfer.setHops(3);
        encxfer.setLayerDepth(2);
        encxfer.setPayload(null);
        EncodedTransferFromNode xfer = new EncodedTransferFromNode();
        xfer.setSourceNode(hello);
        xfer.setSourceNodeInfo(info);
        xfer.setTransfer(encxfer);
        File dri = (File) Codec.getSearchReturnInstr(20, null);
        NodeHello[] hl = new NodeHello[2];
        hl[0] = Codec.CloneNode(MyNode.getNode());
        hl[1] = Codec.CloneNode(MyNode.getNode());
        Object symkey1 = Codec.getNewSymmetricKey();
        File enc1file = (File) Codec.EncodeNodeRoute(dri, symkey1, hl);
        Object symkey2 = Codec.getNewSymmetricKey();
        File origonion = (File) Codec.EncodeNodeRoute(enc1file, symkey2, hl);
        Object returnkey = Codec.getNewSymmetricKey();
        SearchRequest sreq = new SearchRequest();
        sreq.setEncodedReturn(origonion);
        sreq.setFullID(1234L);
        sreq.setKey(returnkey);
        File onionfile = (File) Codec.BundleSearch(sreq);
        for (int cnt = 0; cnt < 10; cnt++) {
            int idx = (int) ((double) GroupPublicKeys.size() * Rand.nextDouble());
            Object enckey = GroupPublicKeys.get(idx);
            onionfile = (File) Codec.EncodeDDR(onionfile, Codec.getNewSymmetricKey(), enckey);
        }
        long StartTime = (new Date()).getTime();
        for (int cnt = 0; cnt < 10; cnt++) {
            xfer.getTransfer().setPayload(onionfile);
            Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
            if (DecodedDDRRoute != null) {
                onionfile = (File) DecodedDDRRoute.getDecodedData();
                ResetAll();
            } else if (SearchQuery != null) {
                if ((Long) SearchQuery.getFullID() == 1234L && cnt == 9) {
                    System.out.println("DDR Decode Passed!");
                    LinkedList<LocalSearchData> rl = new LinkedList<LocalSearchData>();
                    LocalSearchData lsd = new LocalSearchData();
                    SearchData sd = new SearchData();
                    sd.setData(f);
                    sd.setDepth(5);
                    sd.setFullID(1234L);
                    lsd.setSearchData(sd);
                    rl.add(lsd);
                    Object onion = Codec.BundleSearchQueryReturn(rl, SearchQuery.getKey(), SearchQuery.getEncodedReturn());
                    ResetAll();
                    onion = Codec.EncodeNodeRoute(onion, Codec.getNewSymmetricKey(), hl);
                    xfer.getTransfer().setPayload(onion);
                    Codec.ProcessNodeTransfer(onion, this);
                    if (SendRoute != null) {
                        xfer.getTransfer().setPayload(SendRoute.getPayload());
                        ResetAll();
                        Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                        if (SendRoute != null) {
                            RouteTransfer sr = SendRoute;
                            ResetAll();
                            sr.Failure();
                            if (SendRoute != null) {
                                xfer.getTransfer().setPayload(SendRoute.getPayload());
                                ResetAll();
                                Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                                if (SendRoute != null) {
                                    xfer.getTransfer().setPayload(SendRoute.getPayload());
                                    ResetAll();
                                    Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                                    if (SearchReturn != null) {
                                        if (SearchReturn.getID() == 20) {
                                            File dr = (File) SearchReturn.getData();
                                            dri = (File) Codec.getSearchReturnInstr(20, dr);
                                            enc1file = (File) Codec.EncodeNodeRoute(dri, symkey1, hl);
                                            enc1file = (File) Codec.EncodeNodeRoute(enc1file, symkey2, hl);
                                            File rfile = (File) Codec.ExtractAttachedData(origonion, enc1file, returnkey);
                                            List sdlist = Codec.ExtractSearchData(rfile);
                                            if (sdlist.size() != 1) {
                                                System.out.println("7) Search return list is wrong size!");
                                                pass = false;
                                            } else {
                                                File testfile = (File) sdlist.get(0);
                                                if (DiffFiles.diffFiles(testfile, f)) {
                                                    System.out.println("SEARCH REQUEST PASSED!");
                                                } else {
                                                    System.out.println("8) Incorrect search data!");
                                                    pass = false;
                                                }
                                            }
                                        } else {
                                            System.out.println("6) Return ID did not match!");
                                            CheckSet();
                                            pass = false;
                                        }
                                    } else {
                                        System.out.println("5) Search return failed!");
                                        CheckSet();
                                        pass = false;
                                    }
                                } else {
                                    System.out.println("4) Failed Search return!");
                                    CheckSet();
                                    pass = false;
                                }
                            } else {
                                System.out.println("3) Failed failed route transfer on search return!");
                                CheckSet();
                                pass = false;
                            }
                        } else {
                            System.out.println("2) Failed search return!");
                            CheckSet();
                            pass = false;
                        }
                    } else {
                        System.out.println("1) Failed search return!");
                        CheckSet();
                        pass = false;
                    }
                    ResetAll();
                } else {
                    System.out.println("DDR Decode failed! tag: " + DataQuery.getTag() + " cnt: " + cnt);
                    pass = false;
                }
            } else {
                System.out.println("DDR Decode failed!");
                pass = false;
                CheckSet();
            }
            ResetAll();
        }
        long EndTime = (new Date()).getTime();
        long SecondsElapsed = (EndTime - StartTime) / 1000;
        System.out.println("Seconds for decode: " + SecondsElapsed);
        return pass;
    }

    @SuppressWarnings("unchecked")
    public boolean SearchStoreTest() throws IOException {
        boolean pass = true;
        File f = getTestFile();
        NodeHello hello = new NodeHello();
        NodeInfo info = new NodeInfo();
        info.setHello(hello);
        EncodedTransfer encxfer = new EncodedTransfer();
        encxfer.setHops(3);
        encxfer.setLayerDepth(2);
        encxfer.setPayload(null);
        EncodedTransferFromNode xfer = new EncodedTransferFromNode();
        xfer.setSourceNode(hello);
        xfer.setSourceNodeInfo(info);
        xfer.setTransfer(encxfer);
        SearchData sd = new SearchData();
        sd.setData(f);
        sd.setDepth(10);
        sd.setFullID(1234L);
        File onionfile = (File) Codec.BundleSearchStore(sd);
        for (int cnt = 0; cnt < 10; cnt++) {
            int idx = (int) ((double) GroupPublicKeys.size() * Rand.nextDouble());
            Object enckey = GroupPublicKeys.get(idx);
            onionfile = (File) Codec.EncodeDDR(onionfile, Codec.getNewSymmetricKey(), enckey);
        }
        long StartTime = (new Date()).getTime();
        for (int cnt = 0; cnt < 10; cnt++) {
            xfer.getTransfer().setPayload(onionfile);
            Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
            if (DecodedDDRRoute != null) {
                onionfile = (File) DecodedDDRRoute.getDecodedData();
                ResetAll();
            } else if (StoreSearch != null) {
                pass = StoreSearch.getDepth() == 10;
                pass = pass && ((Long) StoreSearch.getFullID() == 1234L);
                pass = DiffFiles.diffFiles(f, (File) StoreSearch.getData());
                if (pass) {
                    System.out.println("Search Store PASSED!!");
                } else {
                    System.out.println("Search Store FAILED!!");
                }
                ResetAll();
            } else {
                System.out.println("DDR Decode failed!");
                pass = false;
                CheckSet();
            }
            ResetAll();
        }
        long EndTime = (new Date()).getTime();
        long SecondsElapsed = (EndTime - StartTime) / 1000;
        System.out.println("Seconds for decode: " + SecondsElapsed);
        return pass;
    }

    @SuppressWarnings("unchecked")
    public boolean SearchSpecTest() throws IOException {
        boolean pass = true;
        NodeHello[] hl = new NodeHello[2];
        hl[0] = Codec.CloneNode(MyNode.getNode());
        hl[1] = Codec.CloneNode(MyNode.getNode());
        NodeHello hello = new NodeHello();
        NodeInfo info = new NodeInfo();
        info.setHello(hello);
        EncodedTransfer encxfer = new EncodedTransfer();
        encxfer.setHops(3);
        encxfer.setLayerDepth(2);
        encxfer.setPayload(null);
        EncodedTransferFromNode xfer = new EncodedTransferFromNode();
        xfer.setSourceNode(hello);
        xfer.setSourceNodeInfo(info);
        xfer.setTransfer(encxfer);
        SearchSpecification ss = new SearchSpecification();
        ss.setGroupKeys(new LinkedList());
        for (int cnt = 0; cnt < 6; cnt++) {
            ss.getGroupKeys().add(GroupPublicKeys.get(cnt));
        }
        File onionfile = (File) Codec.BundleSearchSpec(ss);
        File enc1file = (File) Codec.EncodeNodeRoute(onionfile, Codec.getNewSymmetricKey(), hl);
        enc1file = (File) Codec.EncodeNodeRoute(enc1file, Codec.getNewSymmetricKey(), hl);
        enc1file = (File) Codec.EncodeNodeRoute(enc1file, Codec.getNewSymmetricKey(), hl);
        Codec.ProcessNodeTransfer(enc1file, this);
        if (SendRoute != null) {
            onionfile = (File) SendRoute.getPayload();
            for (int cnt = 0; cnt < 3; cnt++) {
                xfer.getTransfer().setPayload(onionfile);
                Codec.Decode(xfer, MyNode.getPrivateKey(), (List) GroupPrivateKeys, this);
                if (SendRoute != null) {
                    onionfile = (File) SendRoute.getPayload();
                    ResetAll();
                } else if (SearchSpecification != null) {
                    pass = SearchSpecification.getGroupKeys().size() == ss.getGroupKeys().size();
                    for (int cnt2 = 0; cnt2 < ss.getGroupKeys().size() && pass; cnt2++) {
                        ElGamalPublicKeyParameters rpub = (ElGamalPublicKeyParameters) SearchSpecification.getGroupKeys().get(cnt2);
                        ElGamalPublicKeyParameters sspub = (ElGamalPublicKeyParameters) ss.getGroupKeys().get(cnt2);
                        pass = rpub.getY().equals(sspub.getY()) && rpub.getParameters().getP().equals(sspub.getParameters().getP()) && rpub.getParameters().getG().equals(sspub.getParameters().getG());
                    }
                    if (pass) {
                        System.out.println("Search Spec Upload PASSED!!");
                    } else {
                        System.out.println("Search Spec Upload FAILED!");
                    }
                    ResetAll();
                } else {
                    System.out.println("Invalid instruction!");
                    pass = false;
                    CheckSet();
                }
                ResetAll();
            }
        } else {
            System.out.println("Failed to send onion!");
            pass = false;
        }
        return pass;
    }

    @SuppressWarnings("unchecked")
    public boolean TestSearchSpecHash() {
        boolean pass = true;
        SearchSpecification ss = new SearchSpecification();
        ss.setGroupKeys(new LinkedList());
        for (int cnt = 0; cnt < 6; cnt++) {
            ss.getGroupKeys().add(GroupPublicKeys.get(cnt));
        }
        LocalSearchSpecification lss = new LocalSearchSpecification();
        lss.setSpec(ss);
        Codec.CalculateSearchLevel(lss);
        ss.getGroupKeys().remove(5);
        ss.getGroupKeys().add(GroupPublicKeys.get(6));
        LocalSearchSpecification lss1 = new LocalSearchSpecification();
        lss1.setSpec(ss);
        Codec.CalculateSearchLevel(lss1);
        long id = (Long) lss.getFullID();
        System.out.println("LSS ID: " + Long.toHexString(id));
        id = (Long) lss1.getFullID();
        System.out.println("LSS1 ID: " + Long.toHexString(id));
        if (lss1.getFullID().equals(lss.getFullID())) {
            System.out.println("Search Spec Hash check failed!");
            pass = false;
        }
        if (lss1.getLevel().equals(lss.getLevel())) {
            System.out.println("Search Spec Hash level check failed!");
            pass = false;
        }
        return pass;
    }

    public boolean TestLevels() {
        boolean pass = true;
        NodeInfo ni = new NodeInfo();
        ni.setHello(MyNode.getNode());
        Codec.CalculateLevels(ni);
        for (int cnt = 0; cnt < MyNode.getLevels().length; cnt++) {
            System.out.println("L(" + cnt + ") " + MyNode.getLevels()[cnt].getDepth() + " ID: " + Long.toHexString((Long) MyNode.getLevels()[cnt].getID()));
        }
        for (int cnt = 0; cnt < MyNode.getLevels().length; cnt++) {
            System.out.println("L(" + cnt + ") " + ni.getLevels()[cnt].getDepth() + " ID: " + Long.toHexString((Long) ni.getLevels()[cnt].getID()));
            if (!ni.getLevels()[cnt].equals(MyNode.getLevels()[cnt])) {
                System.out.println("LEVELS SHOULD BE EQUAL!");
                pass = false;
            }
        }
        return pass;
    }

    public boolean TestSignatures() {
        boolean pass = true;
        if (!Codec.ValidateKeys(MyNode.getNode())) {
            System.out.println("Failed validate keys!");
            pass = false;
        }
        MyNode.getNode().setHelloNumber(10);
        if (Codec.ValidateKeys(MyNode.getNode())) {
            System.out.println("Failed to fail validation with bad hello number!");
            pass = false;
        }
        Codec.SignMyNodeInfo(MyNode);
        if (!Codec.VerifyNewNodeData(MyNode.getNode(), MyNode.getNode())) {
            System.out.println("Failed to validate new node data!");
            pass = false;
        }
        return pass;
    }

    public boolean TestGroupKeys() {
        boolean pass = true;
        long trys = 1000;
        while (NewGroupKey == null && trys > 0) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            trys--;
        }
        if (NewGroupKey == null) {
            pass = false;
            System.out.println("Failed to generate new group key!");
        } else {
            if (Codec.ValidateGroupKeys(NewGroupKey)) {
                System.out.println("New group key is valid!");
                if (Codec.GroupKeysEqual(NewGroupKey, NewGroupKey)) {
                    System.out.println("Group key equals passed!");
                } else {
                    System.out.println("Gorup key equals failed!");
                    pass = false;
                }
            } else {
                System.out.println("New group key failed to validate!");
                pass = false;
            }
        }
        return pass;
    }

    public void DataQuery(RetrieveRequest request) {
        DataQuery = request;
    }

    public void DecodeFailed(EncodedTransferFromNode xfer) {
        DecodeFailed = xfer;
    }

    public void DecodedDDRRoute(DecodedDDRData xfer) {
        DecodedDDRRoute = xfer;
    }

    public void DownloadReturn(ReturnData data) {
        DownloadReturn = data;
    }

    public void NewData(EncodedTransferFromNode xfer) {
        NewData = xfer;
    }

    public void NewGroupKey(GroupKey newkey) {
        NewGroupKey = newkey;
    }

    public void SearchQuery(SearchRequest request) {
        SearchQuery = request;
    }

    public void SearchReturn(ReturnData data) {
        SearchReturn = data;
    }

    public void SearchSpecification(org.pfyshnet.core.SearchSpecification s) {
        SearchSpecification = s;
    }

    public void SendRoute(RouteTransfer xfer) {
        SendRoute = xfer;
    }

    public void StoreRequest(org.pfyshnet.core.StoreRequest request) {
        StoreRequest = request;
    }

    public void StoreSearch(SearchData data) {
        StoreSearch = data;
    }

    public void UploadReturn(ReturnData data) {
        UploadReturn = data;
    }

    public static void main(String args[]) {
        CodecTester t = new CodecTester();
        t.run();
    }

    public Object getCodecSettings() {
        return null;
    }

    public void setCodecSettings(Object settings) {
    }
}
