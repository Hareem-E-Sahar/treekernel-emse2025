public class Test {    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new HttpUrlConnection(url);
    }
}