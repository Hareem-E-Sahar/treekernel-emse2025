public class Test {    protected URLConnection openConnection(URL url) throws java.io.IOException {
        return new syntelos.net.http.Connection(url);
    }
}