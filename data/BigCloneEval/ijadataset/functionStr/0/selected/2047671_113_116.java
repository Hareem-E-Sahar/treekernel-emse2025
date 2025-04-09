public class Test {    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return NioUtils.getChannel(getStream());
    }
}