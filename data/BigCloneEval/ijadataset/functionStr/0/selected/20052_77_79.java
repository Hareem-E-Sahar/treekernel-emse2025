public class Test {    public PounderData read(URL url) throws Exception {
        return read(url.openStream());
    }
}