public class Test {    @Override
    protected InputStream getInputStream() throws IOException {
        return url.openStream();
    }
}