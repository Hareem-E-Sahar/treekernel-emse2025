public class Test {    private boolean getFileAccess() {
        return (writeLocalFileAccess() && readRemoteFileAccess());
    }
}