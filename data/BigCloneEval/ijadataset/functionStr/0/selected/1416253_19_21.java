public class Test {    public TestDownloadChannel(String dest) throws IOException {
        destFile = new RandomAccessFile(dest, "rw").getChannel();
    }
}