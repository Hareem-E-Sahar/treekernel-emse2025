public class Test {    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return new URLConnectionForATP(url);
    }
}