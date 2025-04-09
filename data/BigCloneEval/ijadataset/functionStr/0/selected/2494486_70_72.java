public class Test {    public SocketOutputStream(Socket socket, long timeout) throws IOException {
        this(socket.getChannel(), timeout);
    }
}