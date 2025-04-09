public class Test {    public InputStream openStream() throws IOException {
        URL url = toURL();
        if (url == null) return null;
        return new BufferedInputStream(url.openStream());
    }
}