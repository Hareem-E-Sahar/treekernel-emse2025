public class Test {    @Override
    public void shutdown() {
        if (writeThread != null) {
            shutdownWriter();
            saveMap();
        }
    }
}