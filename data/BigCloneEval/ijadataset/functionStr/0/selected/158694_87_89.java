public class Test {    public boolean isStopped() {
        return reader.isStopped() || writer.isStopped();
    }
}