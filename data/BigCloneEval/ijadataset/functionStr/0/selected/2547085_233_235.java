public class Test {    protected void writeLock() throws AccessPoemException {
        writeLock(PoemThread.sessionToken());
    }
}