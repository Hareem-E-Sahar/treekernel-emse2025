public class Test {    public void parseStyleSheet(URL url) throws CSSException, IOException {
        parseStyleSheet(url.openStream());
    }
}