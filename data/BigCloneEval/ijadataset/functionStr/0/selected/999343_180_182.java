public class Test {        public OutputStream getOutputStream() throws IOException {
            return url.openConnection().getOutputStream();
        }
}