public class Test {    public boolean numPending() {
        return writePos - readPos != 0;
    }
}