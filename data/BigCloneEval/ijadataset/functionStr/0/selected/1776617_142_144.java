public class Test {    public synchronized void force() throws IOException {
        raf.getChannel().force(true);
    }
}