public class Test {    @Override
    protected URLConnection openConnection(final URL url) {
        return new JavaScriptURLConnection(url);
    }
}