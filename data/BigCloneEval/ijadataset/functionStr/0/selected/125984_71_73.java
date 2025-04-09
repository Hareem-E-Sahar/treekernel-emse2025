public class Test {    public ImagenPersistible(URL url) throws IOException {
        this(url.openStream());
    }
}