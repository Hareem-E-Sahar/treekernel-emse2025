public class Test {    public void connectUrl(String url) throws MalformedURLException, IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.connect();
        connection.getInputStream().close();
        connection.getOutputStream().close();
    }
}