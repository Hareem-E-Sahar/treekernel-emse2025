public class Test {    public void close() {
        if (_read_write) {
            closeWrite();
        } else {
            closeRead();
        }
    }
}