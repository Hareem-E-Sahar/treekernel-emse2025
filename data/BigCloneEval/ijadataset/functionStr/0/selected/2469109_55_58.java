public class Test {    private void connect() throws IOException {
        conn = (HttpURLConnection) url.openConnection();
        getResponseHeaders();
    }
}