public class Test {    public static void readFully(RandomAccessFile src, IntBuffer dest) throws IOException {
        FileChannels.readFully(src.getChannel(), dest);
    }
}