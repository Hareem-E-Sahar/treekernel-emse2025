public class Test {    static final byte[] readStream(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int read;
        byte[] buffer = new byte[256];
        try {
            while ((read = input.read(buffer, 0, 256)) != -1) {
                bytes.write(buffer, 0, read);
            }
        } catch (IOException exception) {
            throw (IOException) exception.fillInStackTrace();
        }
        return bytes.toByteArray();
    }
}