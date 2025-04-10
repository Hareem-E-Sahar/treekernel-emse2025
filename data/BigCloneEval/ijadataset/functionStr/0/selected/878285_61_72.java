public class Test {    public InputStream getInputStream() throws java.io.IOException {
        checkConnection();
        if (!_urlString.endsWith("!/")) return new FilterInputStream(super.getInputStream()) {

            public void close() throws IOException {
                this.in = IO.getClosedStream();
            }
        };
        URL url = new URL(_urlString.substring(4, _urlString.length() - 2));
        InputStream is = url.openStream();
        return is;
    }
}