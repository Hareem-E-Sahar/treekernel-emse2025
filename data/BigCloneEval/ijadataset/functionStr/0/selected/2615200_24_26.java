public class Test {    public URLConnection openConnection(URL url) {
        return new BytesURLConnection(url, content);
    }
}