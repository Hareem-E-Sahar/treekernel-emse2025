public class Test {    public int read(ByteBuffer dest) throws IOException {
        return getChannel().read(dest);
    }
}