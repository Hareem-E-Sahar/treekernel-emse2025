public class Test {    @Override
    public InputStream fetch(URL url) throws IOException {
        return url.openStream();
    }
}