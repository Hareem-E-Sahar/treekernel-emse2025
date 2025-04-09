public class Test {    public static void getChannelList(NetResponseListener listener) {
        forward(listener);
        myHandler.getChannelList();
    }
}