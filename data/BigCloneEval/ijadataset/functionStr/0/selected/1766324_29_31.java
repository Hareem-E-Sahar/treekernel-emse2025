public class Test {    public AbstractURLConnectionTracker(URL url) throws IOException {
        this(url.openConnection());
    }
}