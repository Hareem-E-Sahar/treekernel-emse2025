public class Test {    protected Reader openConnection(URL url) throws IOException {
        return getReader(url.openConnection());
    }
}