public class Test {    public synchronized int available() {
        return writepos - readpos;
    }
}