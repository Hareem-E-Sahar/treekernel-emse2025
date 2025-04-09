public class Test {    private void ensureFileLoaded() {
        if (null != m_text) return;
        StringWriter writer = new StringWriter(2048);
        InputStream in = null;
        Reader reader = null;
        try {
            in = new FileInputStream(m_source);
            reader = new InputStreamReader(in, TaggedOutputStream.DEFAULT_CHARSET);
            char[] temp = new char[2048];
            int read = reader.read(temp);
            while (read > 0) {
                writer.write(temp, 0, read);
                read = reader.read(temp);
            }
            m_text = writer.toString();
        } catch (IOException e) {
            throw new InvalidDataFileFound(m_source.getAbsolutePath(), e);
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
            writer = null;
        }
    }
}