public class Test {    @Override
    public InputStream newInputStream() throws IOException {
        return url.openStream();
    }
}