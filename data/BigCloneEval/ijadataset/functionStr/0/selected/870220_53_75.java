public class Test {    public void connect(final String host, final String port, final String userName, final String password) {
        Thread t = new Thread() {

            public void run() {
                server = new Server(host, port);
                if (monitor != null) monitor.networkActivity(NetworkMonitor.CONNECTING);
                server.connect();
                if (monitor != null) {
                    monitor.networkActivity(NetworkMonitor.CONNECTED);
                    monitor.networkActivity(NetworkMonitor.LOGGING_IN);
                }
                server.login(userName, password);
                if (monitor != null) {
                    monitor.networkActivity(NetworkMonitor.LOGGED_IN);
                    monitor.networkActivity(NetworkMonitor.GETTING_FOLDERS);
                }
                inbox = server.getFolders();
                getSubFolderPaths();
                if (monitor != null) monitor.networkActivity(NetworkMonitor.GOT_FOLDERS);
            }
        };
        t.start();
    }
}