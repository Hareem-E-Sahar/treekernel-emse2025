public class Test {    public Assembly(URL url) throws IOException {
        this(url.openStream());
    }
}