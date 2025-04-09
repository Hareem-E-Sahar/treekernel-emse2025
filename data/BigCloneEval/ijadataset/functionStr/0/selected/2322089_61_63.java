public class Test {    protected URLConnection openConnection() throws Exception {
        return ((URL) url.getValue()).openConnection();
    }
}