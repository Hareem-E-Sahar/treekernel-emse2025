public class Test {    private synchronized byte[] getDigest(byte[] val) {
        return md.digest(val);
    }
}