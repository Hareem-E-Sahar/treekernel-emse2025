public class Test {    public HTTPRequest(URL url) throws IOException {
        this(url.openConnection());
    }
}