public class Test {    public boolean isOpen() {
        return readSink.isOpen() && writeSink.isOpen();
    }
}