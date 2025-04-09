public class Test {    public XmlReader(URL url) throws IOException {
        this(url.openConnection());
    }
}