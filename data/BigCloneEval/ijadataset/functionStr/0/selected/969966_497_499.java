public class Test {    public static void readFully(RandomAccessFile src, ShortBuffer dest) throws IOException {
        FileChannels.readFully(src.getChannel(), dest);
    }
}