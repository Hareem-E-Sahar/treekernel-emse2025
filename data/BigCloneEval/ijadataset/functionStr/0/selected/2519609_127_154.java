public class Test {    protected void flushSpi() throws BackingStoreException {
        try {
            if (isRemoved()) {
                dataFile.delete();
            } else {
                directory.mkdirs();
                FileOutputStream fos = null;
                FileLock lock = null;
                try {
                    fos = new FileOutputStream(dataFile);
                    FileChannel channel = fos.getChannel();
                    lock = channel.lock();
                    properties.store(fos, "created by GNU Classpath FileBasedPreferences");
                } finally {
                    try {
                        if (lock != null) lock.release();
                    } catch (IOException _) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException _) {
                    }
                }
            }
        } catch (IOException ioe) {
            throw new BackingStoreException(ioe);
        }
    }
}