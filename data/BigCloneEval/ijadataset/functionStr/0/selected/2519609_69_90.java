public class Test {    private void load() {
        this.properties = new Properties();
        FileInputStream fis = null;
        FileLock lock = null;
        try {
            fis = new FileInputStream(this.dataFile);
            FileChannel channel = fis.getChannel();
            lock = channel.lock(0, Long.MAX_VALUE, true);
            this.properties.load(fis);
        } catch (IOException _) {
            newNode = true;
        } finally {
            try {
                if (lock != null) lock.release();
            } catch (IOException ignore) {
            }
            try {
                if (fis != null) fis.close();
            } catch (IOException ignore) {
            }
        }
    }
}