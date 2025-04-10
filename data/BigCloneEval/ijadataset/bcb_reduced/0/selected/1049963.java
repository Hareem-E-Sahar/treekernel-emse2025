package openjsip.auth;

import openjsip.SipUtils;
import java.security.*;
import java.util.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;

public class DigestServerAuthenticationMethod {

    private String defaultRealm;

    private final Random random;

    private final Hashtable<String, MessageDigest> algorithms;

    /**
     * Default constructor.
     * @param defaultRealm Realm to use when realm part is not specified in authentication headers.
     * @param algorithms List of algorithms that can be used in authentication.
     * @throws NoSuchAlgorithmException If one of algorithms specified in <i>algorithms</i> is not realized in current Java version.
     */
    public DigestServerAuthenticationMethod(String defaultRealm, String[] algorithms) throws NoSuchAlgorithmException {
        this.defaultRealm = defaultRealm;
        this.algorithms = new Hashtable<String, MessageDigest>();
        random = new Random(System.currentTimeMillis());
        for (String algorithm : algorithms) this.algorithms.put(algorithm, MessageDigest.getInstance(algorithm));
    }

    /**
     * @return The default realm that is to be used when no domain is specified in authentication headers.
     */
    public String getDefaultRealm() {
        return defaultRealm;
    }

    /**
     * @return The algorithm that is to be used when no algorithm is specified in authentication headers.
     */
    public String getPreferredAlgorithm() {
        return algorithms.keys().nextElement();
    }

    /**
     * Generate the challenge string.
     * @param algorithm Encryption algorithm. "MD5", for example.
     * @return a generated nonce. Empty string if specified <i>algorithm</i> is not recognized.
     */
    public String generateNonce(String algorithm) {
        MessageDigest messageDigest = algorithms.get(algorithm);
        if (messageDigest == null) return "";
        long time = System.currentTimeMillis();
        long pad = random.nextLong();
        String nonceString = (new Long(time)).toString() + (new Long(pad)).toString();
        byte mdbytes[] = messageDigest.digest(nonceString.getBytes());
        return SipUtils.toHexString(mdbytes);
    }

    /**
     * Actually performs authentication of subscriber.
     * @param authHeader Authroization header from the SIP request.
     * @param request Request to authorize
     * @param user Username to check with
     * @param password to check with
     * @return true if request is authorized, false in other case.
     */
    public boolean doAuthenticate(Request request, AuthorizationHeader authHeader, String user, String password) {
        String username = authHeader.getUsername();
        if (username == null || !username.equals(user)) return false;
        String realm = authHeader.getRealm();
        if (realm == null) realm = defaultRealm;
        URI uri = authHeader.getURI();
        if (uri == null) return false;
        String algorithm = authHeader.getAlgorithm();
        if (algorithm == null) algorithm = getPreferredAlgorithm();
        MessageDigest messageDigest = algorithms.get(algorithm);
        if (messageDigest == null) return false;
        byte mdbytes[];
        String A1 = username + ":" + realm + ":" + password;
        String A2 = request.getMethod().toUpperCase() + ":" + uri.toString();
        mdbytes = messageDigest.digest(A1.getBytes());
        String HA1 = SipUtils.toHexString(mdbytes);
        mdbytes = messageDigest.digest(A2.getBytes());
        String HA2 = SipUtils.toHexString(mdbytes);
        String nonce = authHeader.getNonce();
        String cnonce = authHeader.getCNonce();
        String KD = HA1 + ":" + nonce;
        if (cnonce != null) KD += ":" + cnonce;
        KD += ":" + HA2;
        mdbytes = messageDigest.digest(KD.getBytes());
        String mdString = SipUtils.toHexString(mdbytes);
        String response = authHeader.getResponse();
        return mdString.compareTo(response) == 0;
    }
}
