public class Test {    @Override
    public byte[] encode(final byte[] data) {
        return this.digest(data);
    }
}