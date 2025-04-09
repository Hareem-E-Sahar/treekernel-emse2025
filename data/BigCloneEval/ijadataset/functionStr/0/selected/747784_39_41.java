public class Test {    public static String getChannel() {
        return SyncContext.getInstance().getServerSource();
    }
}