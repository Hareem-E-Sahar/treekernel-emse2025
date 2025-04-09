public class Test {    public int bytesAvailable() {
        return writeCursor - readCursor;
    }
}