public class Test {    public synchronized CipherParameters getParameters() {
        return getChannel().getParameters();
    }
}