public class Test {    protected HttpURLConnection createConnection() throws IOException {
        return (HttpURLConnection) url.openConnection();
    }
}