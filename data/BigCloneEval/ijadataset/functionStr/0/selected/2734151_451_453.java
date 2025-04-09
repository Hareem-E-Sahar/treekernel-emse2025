public class Test {    public boolean isOpen() {
        return readSink != null && readSink.isOpen() && writeSink != null && writeSink.isOpen();
    }
}