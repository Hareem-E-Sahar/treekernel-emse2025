public class Test {    public byte[] read(URL url) throws IOException {
        return read(url.openStream());
    }
}