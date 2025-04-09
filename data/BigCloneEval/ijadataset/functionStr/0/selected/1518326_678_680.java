public class Test {    public List buildList(URL url) throws IOException {
        return buildList(classLoader.parseClass(url.openStream()));
    }
}