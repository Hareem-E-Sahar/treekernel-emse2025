public class Test {    public void flush() throws IOException {
        raf.getChannel().force(true);
    }
}