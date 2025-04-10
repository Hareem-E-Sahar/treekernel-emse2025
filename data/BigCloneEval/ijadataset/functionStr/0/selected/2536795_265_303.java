public class Test {    boolean doRSAAuth(String userName, BigInteger pubKeyN) throws IOException {
        SSHRSAPublicKeyFile keyFile;
        SSHPduOutputStream outpdu;
        SSHPduInputStream inpdu;
        boolean authenticated = false;
        keyFile = SSHRSAPublicKeyFile.loadFromFile(authKeysDir + userName, false);
        RSAPublicKey pubKey = keyFile.getPublic(pubKeyN, userName);
        if (pubKey == null) return false;
        RSACipher rsa = new RSACipher(new KeyPair(pubKey, null));
        byte[] challenge = new byte[32];
        byte[] tmp;
        BigInteger enc;
        secureRandom().nextBytes(challenge);
        tmp = new byte[challenge.length + 1];
        System.arraycopy(challenge, 0, tmp, 1, challenge.length);
        enc = new BigInteger(tmp);
        enc = rsa.doPad(enc, pubKey.bitLength(), secureRandom());
        enc = rsa.doPublic(enc);
        outpdu = new SSHPduOutputStream(SMSG_AUTH_RSA_CHALLENGE, sndCipher);
        outpdu.writeBigInteger(enc);
        outpdu.writeTo(sshOut);
        inpdu = new SSHPduInputStream(CMSG_AUTH_RSA_RESPONSE, rcvCipher);
        inpdu.readFrom(sshIn);
        tmp = new byte[16];
        inpdu.read(tmp, 0, 16);
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(challenge, 0, 32);
            md5.update(sessionId);
            challenge = md5.digest();
        } catch (Exception e) {
            System.out.println("!!! MD5 Not supported...");
            throw new IOException(e.getMessage());
        }
        int i;
        for (i = 0; i < challenge.length; i++) if (tmp[i] != challenge[i]) break;
        if (i == challenge.length) authenticated = true;
        return authenticated;
    }
}