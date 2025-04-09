public class Test {    protected synchronized int available() {
        return writepos - readpos;
    }
}