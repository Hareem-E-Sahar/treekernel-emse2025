public class Test {    public java.nio.channels.FileChannel getChannel() {
        try {
            java.io.File cache = java.io.File.createTempFile("vfs", ".channel");
            return new FileChannel(file, new java.io.FileInputStream(cache).getChannel());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}