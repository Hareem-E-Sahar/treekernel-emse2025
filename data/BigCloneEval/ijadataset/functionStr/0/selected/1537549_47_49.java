public class Test {    public DocumentReader(URL url) throws IOException {
        fInput = new InputSource(url.openStream());
    }
}