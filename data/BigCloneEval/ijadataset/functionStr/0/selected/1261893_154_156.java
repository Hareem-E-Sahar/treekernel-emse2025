public class Test {    protected InputStream getInputStream(URL url) throws IOException {
        return url.openStream();
    }
}