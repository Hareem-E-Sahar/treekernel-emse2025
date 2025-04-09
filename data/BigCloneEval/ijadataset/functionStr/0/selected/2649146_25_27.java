public class Test {    public URLConnection openConnection(URL url) throws IOException {
        return new HTTPURLConnection(url);
    }
}