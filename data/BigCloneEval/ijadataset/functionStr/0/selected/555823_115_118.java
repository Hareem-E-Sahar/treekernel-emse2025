public class Test {    HttpURLConnection getHttpConnection() throws IOException {
        URL url = new URL(getTSAUrl());
        return (HttpURLConnection) url.openConnection();
    }
}