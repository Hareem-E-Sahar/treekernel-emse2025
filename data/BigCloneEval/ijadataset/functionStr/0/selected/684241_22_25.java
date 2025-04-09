public class Test {    InputStream open() throws IOException {
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }
}