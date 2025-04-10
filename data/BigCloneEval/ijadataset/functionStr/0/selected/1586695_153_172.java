public class Test {    private static void acceptClient(final int clientId, final Socket socket) {
        Runnable runnable = new Runnable() {

            public void run() {
                try {
                    System.out.println("AcceptClient[" + clientId + "] created: " + socket);
                    int data;
                    InputStream in = socket.getInputStream();
                    OutputStream out = socket.getOutputStream();
                    while ((data = in.read()) > 0) out.write(data);
                    out.flush();
                    socket.close();
                    System.out.println("AcceptClient[" + clientId + "] closed: " + socket);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        new Thread(runnable, "AcceptCleint[" + clientId + "]").start();
    }
}