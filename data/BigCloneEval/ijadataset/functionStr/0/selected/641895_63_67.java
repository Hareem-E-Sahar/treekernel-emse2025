public class Test {        private void initDelegate() throws IOException {
            if (delegateStream == null) {
                delegateStream = url.openStream();
            }
        }
}