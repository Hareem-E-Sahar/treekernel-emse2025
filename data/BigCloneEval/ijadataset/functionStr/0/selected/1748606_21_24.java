public class Test {    public byte[] getDigest() throws NoSuchAlgorithmException {
        MessageDigest digest = CMSSignedHelper.INSTANCE.getDigestInstance(alg, provider);
        return digest.digest(data);
    }
}