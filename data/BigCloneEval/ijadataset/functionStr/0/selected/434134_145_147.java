public class Test {    public Workbench(URL url) throws Exception {
        this(loadCrawler(url.openStream()));
    }
}