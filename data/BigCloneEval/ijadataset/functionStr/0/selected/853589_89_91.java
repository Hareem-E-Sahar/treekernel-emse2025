public class Test {    public AImage(URL url) throws IOException {
        this(getName(url), url.openStream());
    }
}