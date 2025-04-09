public class Test {    public StreamIterator(URL url) throws IOException {
        this(url.openStream());
    }
}