public class Test {        int getContentLength() throws IOException {
            return _url.openConnection().getContentLength();
        }
}