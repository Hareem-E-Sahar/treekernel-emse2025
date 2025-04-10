public class Test {    public void changeNetworkLimit(long writeLimit, long readLimit) {
        long newWriteLimit = writeLimit > 1024 ? writeLimit : serverGlobalWriteLimit;
        if (writeLimit <= 0) {
            newWriteLimit = 0;
        }
        long newReadLimit = readLimit > 1024 ? readLimit : serverGlobalReadLimit;
        if (readLimit <= 0) {
            newReadLimit = 0;
        }
        globalTrafficShapingHandler.configure(newWriteLimit, newReadLimit);
        serverChannelReadLimit = newReadLimit / 10;
        serverChannelWriteLimit = newWriteLimit / 10;
    }
}