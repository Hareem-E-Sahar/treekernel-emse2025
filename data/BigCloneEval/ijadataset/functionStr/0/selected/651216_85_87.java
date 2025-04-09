public class Test {    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        throw nonWritableChannelException();
    }
}