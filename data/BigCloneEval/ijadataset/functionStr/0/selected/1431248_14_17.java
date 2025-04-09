public class Test {    @Override
    protected void send(String messageToGUI) {
        getChannel().sendLogToGUI(messageToGUI);
    }
}