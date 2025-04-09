public class Test {    public static void connectAndLogin() throws XMPPException {
        connection = new JavverConnection();
        connection.connect();
        connection.login();
    }
}