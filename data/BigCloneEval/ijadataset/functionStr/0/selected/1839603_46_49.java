public class Test {    @Override
    public synchronized void begin() {
        begin(Mode.readwrite);
    }
}