public class Test {        FileOut() throws IOException {
            file = File.createTempFile("test", "test");
            channel = new RandomAccessFile(file, "rw").getChannel();
        }
}