public class Test {    public OutputStream getOutputStream() throws IOException {
        return getChannel().getOutputStream();
    }
}