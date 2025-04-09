public class Test {    public HashWrapper getHash() {
        return new HashWrapper(sha1.digest());
    }
}