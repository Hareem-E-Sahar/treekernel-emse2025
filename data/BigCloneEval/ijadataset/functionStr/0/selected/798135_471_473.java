public class Test {    public boolean isWritable() {
        return (!readOnly && writeLock.isLocked());
    }
}