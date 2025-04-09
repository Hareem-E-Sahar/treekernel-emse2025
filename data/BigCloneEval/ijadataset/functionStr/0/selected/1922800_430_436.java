public class Test {    public void sendsToServerClientSpacecraft(String message) {
        try {
            threadCommunication.os.writeUTF("clientToServerSpacecraft/" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}