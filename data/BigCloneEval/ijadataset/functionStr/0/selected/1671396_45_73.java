public class Test {    public static String readFromFile(File file) throws IOException {
        StringWriter writer = new StringWriter(2048);
        InputStream in = null;
        Reader reader = null;
        try {
            in = new FileInputStream(file);
            reader = new InputStreamReader(in, TaggedOutputStream.DEFAULT_CHARSET);
            char[] temp = new char[2048];
            int read = reader.read(temp);
            while (read > 0) {
                writer.write(temp, 0, read);
                read = reader.read(temp);
            }
            return writer.toString();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException _) {
                }
            }
            if (null != in) {
                try {
                    in.close();
                } catch (IOException _) {
                }
            }
        }
    }
}