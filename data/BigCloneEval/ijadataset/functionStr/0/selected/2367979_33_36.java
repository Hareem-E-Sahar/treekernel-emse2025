public class Test {    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return ByteUtils.getChannel(this);
    }
}