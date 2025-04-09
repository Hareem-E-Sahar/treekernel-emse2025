public class Test {    protected OutputStream getOutputStream() throws IOException {
        return url.openConnection().getOutputStream();
    }
}