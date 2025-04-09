public class Test {    @Override
    public URLConnection openConnection(URL url) {
        return new PpURLConnection(url);
    }
}