public class Test {    public Tree loadTree(URL url) throws IOException {
        return loadTree(url.openStream());
    }
}