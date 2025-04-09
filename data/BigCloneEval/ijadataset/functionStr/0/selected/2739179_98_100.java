public class Test {    public ClientHttpRequest(URL url) throws IOException {
        this((HttpURLConnection) url.openConnection());
    }
}