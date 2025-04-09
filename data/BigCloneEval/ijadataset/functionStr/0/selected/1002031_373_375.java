public class Test {    public void sendNotice(final NOTICE notice) {
        sendRaw("NOTICE " + notice.getChannel() + " :" + notice.getMessage() + "\n");
    }
}