public class Test {    protected URLConnection openConnection(final URL url) throws IOException {
        return new CaptureURLConnection(url);
    }
}