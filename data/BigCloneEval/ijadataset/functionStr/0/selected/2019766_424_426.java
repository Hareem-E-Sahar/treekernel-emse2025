public class Test {    public static PDDocument load(URL url) throws IOException {
        return load(url.openStream());
    }
}