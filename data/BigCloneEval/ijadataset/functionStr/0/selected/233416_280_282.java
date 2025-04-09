public class Test {        InputStream getContent() throws IOException {
            return _url.openConnection().getInputStream();
        }
}