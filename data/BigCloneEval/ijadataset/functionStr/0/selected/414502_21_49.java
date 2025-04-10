public class Test {    private static boolean loadFromClassPath(String filename) {
        File file = new File(JNI_TMP_PATH + filename);
        try {
            if (!file.exists()) {
                InputStream inputStream = TGClassLoader.instance().getClassLoader().getResourceAsStream(filename);
                if (inputStream != null) {
                    OutputStream outputStream = new FileOutputStream(file);
                    int read;
                    byte[] buffer = new byte[4096];
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    outputStream.close();
                    inputStream.close();
                }
            }
            if (file.exists()) {
                System.load(file.getAbsolutePath());
                return true;
            }
        } catch (Throwable throwable) {
            return false;
        } finally {
            if (file.exists()) {
                file.delete();
            }
        }
        return false;
    }
}