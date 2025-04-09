package fairVote.agent.registrar;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import fairVote.agent.AgentCore;
import fairVote.core.MyException;
import fairVote.panel.AuthManager;
import fairVote.util.Basic;
import fairVote.util.Crypto;
import fairVote.util.Debug;
import fairVote.util.FairLog;
import fairVote.util.KeyGen;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class RegistrarClient {

    private static Logger LOGGER = FairLog.getLogger(RegistrarClient.class.getName());

    private String registrarServerUrl;

    private Certificate registrarServerCert;

    private PrivateKey key;

    private Certificate cert;

    private AuthManager authMan;

    private String tyAuth;

    private KeyPair keyPairTmp;

    private String S1;

    public int errno = 0;

    public RegistrarClient(String authServer, Certificate certServer, PrivateKey key, Certificate cert) {
        this.registrarServerUrl = authServer;
        this.registrarServerCert = certServer;
        this.key = key;
        this.cert = cert;
        this.keyPairTmp = null;
        this.S1 = null;
        this.tyAuth = "X509";
    }

    public RegistrarClient(String authServer, Certificate certServer, AuthManager authMan) {
        this.registrarServerUrl = authServer;
        this.registrarServerCert = certServer;
        this.authMan = authMan;
        this.keyPairTmp = null;
        this.S1 = null;
        this.tyAuth = "USERPASS";
    }

    public String openSession(String IDVotazione) {
        System.out.println("Start session for " + IDVotazione);
        S1 = null;
        keyPairTmp = null;
        String result = "OK";
        try {
            System.out.println("Generate a key pair for cripting");
            keyPairTmp = Crypto.generateKeyPair(512);
            PublicKey pp = keyPairTmp.getPublic();
            System.out.println("Generate S1");
            KeyGen kg = new KeyGen();
            S1 = kg.getRandomKey(Registrar.SessionTokenSize);
            if (Debug.debuglevel > 2) System.out.println("Prepare 1st msg of challenge");
            byte[] bundle_auth = null;
            if (tyAuth.equals("X509")) {
                byte[] certBytes = cert.getEncoded();
                bundle_auth = AgentCore.createMsg("X509".getBytes(), certBytes);
            } else if (tyAuth.equals(Registrar.C_AUTHUSERPASS)) {
                bundle_auth = AgentCore.createMsg(Registrar.C_AUTHUSERPASS.getBytes(), this.authMan.toString().getBytes());
            }
            byte[] bundle = AgentCore.createMsg(S1.getBytes(), bundle_auth, pp.getEncoded(), IDVotazione.getBytes());
            byte[] signBundle = null;
            if (Debug.debuglevel > 2) System.out.println("Sign bundle");
            if (tyAuth.equals("X509")) {
                signBundle = Crypto.sign(bundle, key);
            } else {
                signBundle = "NONE".getBytes();
            }
            if (Debug.debuglevel > 2) System.out.println("prepare 1st msg");
            byte[] msg = AgentCore.createMsg(bundle, signBundle);
            byte[] encBytes = Crypto.encrypt(msg, registrarServerCert.getPublicKey());
            System.out.println("Send 1st msg to registrar");
            String sauthServer = registrarServerUrl + "/registrar/openSession";
            String response = AgentCore.sendBytes(encBytes, sauthServer);
            System.out.println("response:" + response);
            if (response.startsWith("FAIL")) {
                if (response.equals(AgentCore.ERRMSG_VOTATIONLOCKED)) throw new MyException(MyException.ERROR_FASTEXIT, response);
                System.out.println("CHECKTHIS[" + response + "]");
                throw new Exception("Registrar refuse 1st msg");
            }
            System.out.println("Send Ok");
            byte[] msgR = Base64.decodeBase64(response.getBytes("utf-8"));
            if (Debug.debuglevel > 2) System.out.println("Descramble msgR");
            msg = Crypto.decrypt(msgR, keyPairTmp.getPrivate(), "RSA", 50, 64);
            byte[] bS1R = AgentCore.getS1FromMsg(msg);
            byte[] signS1R = AgentCore.getS2FromMsg(msg);
            System.out.println("Check S1R");
            if (!Arrays.equals(S1.getBytes(), bS1R)) throw new Exception("S1R differs from S1");
            boolean check = Crypto.verifySign(S1.getBytes(), signS1R, registrarServerCert.getPublicKey());
            if (!check) throw new Exception("S1R sign verification filed");
        } catch (IOException e) {
            result = AgentCore.ERRMSG_IO;
        } catch (MyException e) {
            if (e.getErrType() == MyException.ERROR_FASTEXIT) result = e.getErrMsg(); else result = "FAIL::" + e.getErrAsString();
        } catch (Exception e) {
            LOGGER.error("Error opening session", e);
            result = "FAIL";
        }
        System.out.println("End OpenSession: " + result);
        return result;
    }

    public static final int C_TIMETOOLATE = 1;

    public static final int C_TIMETOOEARLY = 2;

    public static final int C_NOTELIGIBLE = 3;

    public byte[] getT1(String IDVotazione) {
        byte[] T1Bundle = null;
        String result = "OK";
        try {
            System.out.println("Create request for T1");
            byte[] msg = AgentCore.createMsg(S1.getBytes(), IDVotazione.getBytes());
            if (Debug.debuglevel > 2) System.out.println("Scramble msg");
            byte[] encBytes = Crypto.encrypt(msg, registrarServerCert.getPublicKey());
            System.out.println("Send request for T1");
            String sauthServer = registrarServerUrl + "/registrar/getT1";
            String response = AgentCore.sendBytes(encBytes, sauthServer);
            System.out.println("Send Ok");
            if (Debug.debuglevel > 2) System.out.println(response);
            if (response.startsWith("FAIL")) {
                if (response.startsWith("FAIL::TimeTooLate")) {
                    errno = C_TIMETOOLATE;
                    throw new Exception("OK");
                }
                if (response.startsWith("FAIL::TimeTooEarly")) {
                    errno = C_TIMETOOEARLY;
                    ;
                    throw new Exception("OK");
                }
                if (response.equals(AgentCore.ERRMSG_NOTELIGIBLE)) {
                    errno = C_NOTELIGIBLE;
                    throw new Exception("OK");
                }
                throw new Exception("response::" + response);
            }
            if (Debug.debuglevel > 2) System.out.println("Decript Msg");
            byte[] dataenc = Base64.decodeBase64(response.getBytes("utf-8"));
            byte[] msgT1 = Crypto.decrypt(dataenc, keyPairTmp.getPrivate(), "RSA", 50, 64);
            System.out.println("extract T1");
            byte[] bTS1 = AgentCore.getS1FromMsg(msgT1);
            String TS1 = Basic.byte2String(bTS1);
            byte[] T1 = AgentCore.getS2FromMsg(msgT1);
            byte[] signT1 = AgentCore.getS3FromMsg(msgT1);
            if (!S1.equals(TS1)) {
                if (S1 == null) System.out.println("S1:null"); else System.out.println("S1:" + S1);
                if (TS1 == null) System.out.println("TS1:null"); else System.out.println("TS1:" + TS1);
                throw new Exception("S1 and TS1 differs");
            }
            if (!Crypto.verifySign(T1, signT1, registrarServerCert.getPublicKey())) throw new Exception("T1 signature fault"); else System.out.println("T1 sign ok");
            T1Bundle = AgentCore.createMsg(T1, signT1);
        } catch (Exception e) {
            if (!e.getMessage().equals("OK")) {
                e.printStackTrace();
                errno = -1;
            }
            return null;
        }
        System.out.println("End of getT1: " + result);
        if (result.equals("FAIL")) {
            errno = -1;
            return null;
        }
        errno = 0;
        return T1Bundle;
    }

    public int closeSession() {
        String result = "OK";
        try {
            System.out.println("Close Session");
            if (Debug.debuglevel > 2) System.out.println("Scramble msg");
            byte[] encBytes = Crypto.encrypt(S1.getBytes(), registrarServerCert.getPublicKey());
            System.out.println("Send request to kill session");
            String sauthServer = registrarServerUrl + "/registrar/closeSession";
            String response = AgentCore.sendBytes(encBytes, sauthServer);
            if (Debug.debuglevel > 2) System.out.println("Analize Response");
            if (response.startsWith("FAIL")) {
                throw new Exception("response:" + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = "FAIL";
        }
        System.out.println("Close Session [result:" + result + "]");
        if (result.equals("FAIL")) return -1;
        return 0;
    }

    public byte[] createT2(byte[] T1) {
        byte[] T2 = null;
        String result = "OK";
        try {
            if (tyAuth.equals("X509")) {
                T2 = Registrar.createT2(T1, cert.getEncoded());
            } else if (tyAuth.equals("USERPASS")) {
                T2 = Registrar.createT2(T1, Basic.byte2HexString(T1).getBytes());
            } else {
                throw new Exception("Auth unknow");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = "FAIL";
        }
        System.out.println("End of Create T2: " + result);
        if (result.equals("FAIL")) return null;
        return T2;
    }

    public String sendT2(byte[] T2) {
        String result = "BOH";
        try {
            byte[] msg = AgentCore.createMsg(S1.getBytes(), T2);
            byte[] encBytes = Crypto.encrypt(msg, registrarServerCert.getPublicKey());
            System.out.println("Send T2::2");
            String sauthServer = registrarServerUrl + "/registrar/sendT2";
            String response = AgentCore.sendBytes(encBytes, sauthServer);
            result = response;
        } catch (Exception e) {
            e.printStackTrace();
            result = "FAIL";
        }
        System.out.println("End of Send T2 [result:" + result + "]");
        return result;
    }

    public boolean checkVote(byte[] T1, byte[] DVotazioneSalt, byte[] DVotazioneSaltT2, String IDVotazione) {
        try {
            byte[] R = AgentCore.createMsg(T1, DVotazioneSalt, DVotazioneSaltT2);
            byte[] signR = Crypto.sign(R, key);
            byte[] Rmsg = AgentCore.createMsg(R, signR, IDVotazione.getBytes());
            LOGGER.trace("Scramble msg[R,signR]");
            byte[] encBytes = Crypto.encrypt(Rmsg, this.registrarServerCert.getPublicKey());
            LOGGER.debug("check with registrar for vote validity");
            String sauthServer = registrarServerUrl + "/registrar/checkVote";
            String response = AgentCore.sendBytes(encBytes, sauthServer);
            LOGGER.trace("response: " + response);
            if (response.equals("FAIL")) throw new Exception("Registrar refuse T2");
        } catch (Exception e) {
            LOGGER.error("Unexpected exception", e);
            return false;
        }
        return true;
    }
}
