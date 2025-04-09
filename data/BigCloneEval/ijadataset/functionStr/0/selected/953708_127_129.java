public class Test {    protected void createReader(URL url) throws IOException {
        reader = initReader(url.openStream());
    }
}