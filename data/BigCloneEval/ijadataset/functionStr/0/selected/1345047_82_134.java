public class Test {    public String generateResponse() {
        if (userName == null) {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): " + "ERROR: no userName parameter");
            return null;
        }
        if (realm == null) {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): " + "ERROR: no realm parameter");
            return null;
        }
        System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): " + "Trying to generate a response for the user: " + userName + " , with " + "the realm: " + realm);
        if (password == null) {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): " + "ERROR: no password parameter");
            return null;
        }
        if (method == null) {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): " + "ERROR: no method parameter");
            return null;
        }
        if (uri == null) {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): " + "ERROR: no uri parameter");
            return null;
        }
        if (nonce == null) {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): " + "ERROR: no nonce parameter");
            return null;
        }
        if (messageDigest == null) {
            System.out.println("DEBUG, DigestClientAuthenticationMethod, generateResponse(): " + "ERROR: the algorithm is not set");
            return null;
        }
        String A1 = userName + ":" + realm + ":" + password;
        byte mdbytes[] = messageDigest.digest(A1.getBytes());
        String HA1 = toHexString(mdbytes);
        String A2 = method.toUpperCase() + ":" + uri;
        mdbytes = messageDigest.digest(A2.getBytes());
        String HA2 = toHexString(mdbytes);
        String KD;
        if (qop_value != null) {
            KD = HA1 + ":" + nonce + ":" + nonce_count;
            if (cnonce != null) {
                if (cnonce.length() > 0) KD += ":" + cnonce;
            }
            KD += ":" + qop_value;
            KD += ":" + HA2;
            mdbytes = messageDigest.digest(KD.getBytes());
        } else {
            KD = HA1 + ":" + nonce + ":" + HA2;
            mdbytes = messageDigest.digest(KD.getBytes());
        }
        String response = toHexString(mdbytes);
        System.out.println("DEBUG, DigestClientAlgorithm, generateResponse():" + " response generated: " + response);
        return response;
    }
}