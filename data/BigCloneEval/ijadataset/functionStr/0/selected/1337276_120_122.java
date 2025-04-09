public class Test {    private boolean getFileStatus() {
        return (writeLocalFileStatus() && readRemoteFileStatus());
    }
}