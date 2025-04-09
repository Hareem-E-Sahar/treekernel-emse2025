public class Test {    public final boolean processAtServer() {
        synchronized (streamLock()) {
            transaction().writeUpdateDeleteMembers(readInt(), stream().classMetadataForId(readInt()), readInt(), readInt());
        }
        return true;
    }
}