public class Test {    InputStream getChangeSetStream(long id) throws IOException {
        URL url = getChangeSetURL(id);
        return url.openStream();
    }
}