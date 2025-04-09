public class Test {    public Document parse(URL url) throws IOException, SAXException {
        return parse(url.openStream());
    }
}