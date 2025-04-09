public class Test {    public int currentlyInBuffer() {
        assert writePos >= readPos;
        return writePos - readPos;
    }
}