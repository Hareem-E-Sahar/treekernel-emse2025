public class Test {    public void flush() throws IOException {
        super.flush();
        getChannel().force(false);
    }
}