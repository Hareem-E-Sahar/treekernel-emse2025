public class Test {    public String getChannelName() {
        return "JETTY_HTTPSESSION_DISTRIBUTION:" + getContextPath() + "-" + getSubClusterName();
    }
}