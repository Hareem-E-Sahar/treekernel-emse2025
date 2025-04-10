public class Test {    protected boolean testPort(String protocol, String server, int port, String file) {
        URL url = null;
        try {
            url = new URL(protocol, server, port, file);
        } catch (MalformedURLException ex) {
            log.severe("No URL for Protocol=" + protocol + ", Server=" + server + ": " + ex.getMessage());
            return false;
        }
        try {
            URLConnection c = url.openConnection();
            Object o = c.getContent();
            log.severe("In use=" + url);
        } catch (Exception ex) {
            log.fine("Not used=" + url);
            return false;
        }
        return true;
    }
}