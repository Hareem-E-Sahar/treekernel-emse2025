public class Test {    public WritableByteChannel openForWriting() throws IOException {
        return new FileOutputStream(file).getChannel();
    }
}