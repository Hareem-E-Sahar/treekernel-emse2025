public class Test {    public void newStream(URL urlRead) throws IOException {
        newStream((HttpURLConnection) urlRead.openConnection());
    }
}