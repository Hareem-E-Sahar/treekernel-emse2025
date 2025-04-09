public class Test {    @Override
    public void close() {
        if (digest == null) {
            digest = hash.digest();
        }
    }
}