public class Test {    public static Script load(URL url) throws IOException {
        return load(url.openStream());
    }
}