public class Test {        @Override
        public InputStream getInputStream() throws IOException {
            return this.url.openStream();
        }
}