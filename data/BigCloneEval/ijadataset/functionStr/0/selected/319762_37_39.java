public class Test {    public HexFileGroupReader(URL url) throws IOException {
        this(new BufferedReader(new InputStreamReader(url.openStream())));
    }
}