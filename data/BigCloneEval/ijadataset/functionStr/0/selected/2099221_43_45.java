public class Test {    protected void copy(InputStream inputs, OutputStream outputs) throws IOException {
        IOUtils.copy(inputs, outputs);
    }
}