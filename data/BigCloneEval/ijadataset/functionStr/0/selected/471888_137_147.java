public class Test {    public static final void copy(File dst, Reader reader, String charset) throws IOException {
        final File parent = dst.getParentFile();
        if (parent != null) parent.mkdirs();
        final Writer writer = charset != null ? (Writer) new FileWriter(dst, charset) : new java.io.FileWriter(dst);
        try {
            copy(writer, reader);
        } finally {
            close(reader);
            writer.close();
        }
    }
}