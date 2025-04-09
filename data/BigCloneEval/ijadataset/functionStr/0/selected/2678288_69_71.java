public class Test {    public void flush() throws IOException {
        getChannel().force(true);
    }
}