public class Test {    private boolean isSinglePacketPresent() {
        return this.readCursor == this.writeCursor;
    }
}