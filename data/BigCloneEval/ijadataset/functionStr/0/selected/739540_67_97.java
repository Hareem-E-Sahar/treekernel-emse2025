public class Test {    public TSPClient(String host, String user, String password) throws SocketException, UnknownHostException {
        this.user = user;
        this.password = password;
        connection = new DatagramSocket();
        serverAddr = InetAddress.getByName(host);
        receiveP = new DatagramPacket(new byte[1280], 1280);
        Runnable writer = new Runnable() {

            public void run() {
                try {
                    writer();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Runnable reader = new Runnable() {

            public void run() {
                try {
                    reader();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(writer).start();
        new Thread(reader).start();
    }
}