public class Test {    public static URLConnection configureURLConnection(String uri, int connectTimeout, int readtimeout) throws IOException {
        URL url = new URL(uri);
        URLConnection con = url.openConnection();
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readtimeout);
        return con;
    }
}