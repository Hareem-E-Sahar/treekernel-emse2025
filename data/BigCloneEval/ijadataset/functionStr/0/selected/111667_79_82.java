public class Test {    private synchronized byte[] calculateHash(byte[] dataToHash) {
        md.update(dataToHash, 0, dataToHash.length);
        return md.digest();
    }
}