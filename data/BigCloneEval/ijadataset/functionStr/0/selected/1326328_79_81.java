public class Test {    public void updateUser(User user) throws PersistenceException {
        writeMonitorThread.scheduleWriteAction();
    }
}