public class Test {    private void get(MessageEvent e) {
        initDoc(e.getChannel());
        writeFileResponse(e.getChannel());
    }
}