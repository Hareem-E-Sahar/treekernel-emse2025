public class Test {    private void kickOffWebSitesGrabbing(final String urlString, final YahooReaderFilter caller) {
        caller.webSitesGrabbingThread = new Thread() {

            public void run() {
                try {
                    caller.webSitesResults = new HashSet();
                    YahooReaderFilter filter = new YahooReaderFilter(true);
                    URL url = new URL(urlString);
                    Reader r = new InputStreamReader(url.openStream());
                    caller.webSitesResults = filter.extractResults(r);
                } catch (Exception e) {
                    System.err.println("Couldn't retrieve Yahoo's " + "web sites results:");
                    e.printStackTrace();
                }
            }
        };
        caller.webSitesGrabbingThread.start();
    }
}