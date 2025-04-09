public class Test {    public String[] getChannels() throws RemoteException {
        return channels.toArray(new String[channels.size()]);
    }
}