public class Test {    public XmlStreamReader(URL url) throws IOException {
        this(url.openConnection(), null);
    }
}