public class Test {    public void inc(int what) {
        ses[what % ssize].write((short) (ses[what % ssize].read() + 1));
    }
}